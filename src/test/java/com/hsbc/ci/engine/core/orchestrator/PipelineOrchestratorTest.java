package com.hsbc.ci.engine.core.orchestrator;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.PipelineDefinition;
import com.hsbc.ci.engine.core.model.PipelineResult;
import com.hsbc.ci.engine.core.model.StageDefinition;
import com.hsbc.ci.engine.core.model.StageResult;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.stages.StageExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineOrchestratorTest {

    @Mock
    private ConfigurationLoader configLoader;

    @Mock
    private StageExecutor stageExecutor;

    @Mock
    private PluginManager pluginManager;

    private PipelineOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new PipelineOrchestrator(configLoader, stageExecutor, pluginManager);
    }

    @Test
    void execute_returnsFailedResult_whenPipelineNotFound() {
        when(configLoader.loadPipelineDefinition("unknown-pipeline")).thenReturn(null);

        PipelineContext context = PipelineContext.builder()
            .pipelineName("unknown-pipeline")
            .build();

        PipelineResult result = orchestrator.execute(context);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("not found"));
    }

    @Test
    void execute_returnsSuccess_forValidPipeline() {
        PipelineDefinition pipeline = createPipeline("test-pipeline", List.of(
            createStage("build", "build"),
            createStage("test", "test")
        ));
        when(configLoader.loadPipelineDefinition("test-pipeline")).thenReturn(pipeline);
        when(stageExecutor.execute(any(), anyMap(), any())).thenReturn(
            StageResult.builder().stageName("build").success(true).build()
        );

        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .build();

        PipelineResult result = orchestrator.execute(context);

        assertTrue(result.isSuccess());
    }

    @Test
    void execute_returnsFailedResult_whenStageFails() {
        PipelineDefinition pipeline = createPipeline("test-pipeline", List.of(
            createStage("build", "build")
        ));
        when(configLoader.loadPipelineDefinition("test-pipeline")).thenReturn(pipeline);
        when(stageExecutor.execute(any(), anyMap(), any())).thenReturn(
            StageResult.builder().stageName("build").success(false).output("Build failed").build()
        );

        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .build();

        PipelineResult result = orchestrator.execute(context);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("failed"));
    }

    @Test
    void execute_runsDryRun_whenDryRunEnabled() {
        PipelineDefinition pipeline = createPipeline("test-pipeline", List.of(
            createStage("build", "build")
        ));
        when(configLoader.loadPipelineDefinition("test-pipeline")).thenReturn(pipeline);

        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .dryRun(true)
            .build();

        PipelineResult result = orchestrator.execute(context);

        assertTrue(result.isSuccess());
        verify(stageExecutor, never()).execute(any(), anyMap(), any());
    }

    @Test
    void execute_retriesFailedStage() {
        PipelineDefinition pipeline = createPipeline("test-pipeline", List.of(
            createStage("build", "build", 2)
        ));
        when(configLoader.loadPipelineDefinition("test-pipeline")).thenReturn(pipeline);
        when(stageExecutor.execute(any(), anyMap(), any()))
            .thenReturn(StageResult.builder().stageName("build").success(false).build())
            .thenReturn(StageResult.builder().stageName("build").success(true).build());

        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .build();

        PipelineResult result = orchestrator.execute(context);

        assertTrue(result.isSuccess());
        verify(stageExecutor, times(2)).execute(any(), anyMap(), any());
    }

    @Test
    void execute_runsPreAndPostStagePlugins() {
        PipelineDefinition pipeline = createPipeline("test-pipeline", List.of(
            createStage("build", "build")
        ));
        when(configLoader.loadPipelineDefinition("test-pipeline")).thenReturn(pipeline);
        when(stageExecutor.execute(any(), anyMap(), any())).thenReturn(
            StageResult.builder().stageName("build").success(true).build()
        );
        when(pluginManager.listStagePlugins()).thenReturn(List.of("security-scan"));

        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .build();

        orchestrator.execute(context);

        verify(pluginManager).listStagePlugins();
    }

    @Test
    void context_containsStageResults_afterExecution() {
        PipelineDefinition pipeline = createPipeline("test-pipeline", List.of(
            createStage("build", "build")
        ));
        when(configLoader.loadPipelineDefinition("test-pipeline")).thenReturn(pipeline);
        when(stageExecutor.execute(any(), anyMap(), any())).thenReturn(
            StageResult.builder()
                .stageName("build")
                .success(true)
                .durationMs(1000)
                .build()
        );

        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .build();

        PipelineResult result = orchestrator.execute(context);

        assertTrue(context.getStageResults().containsKey("build"));
    }

    private PipelineDefinition createPipeline(String name, List<StageDefinition> stages) {
        PipelineDefinition pipeline = new PipelineDefinition();
        pipeline.setName(name);
        pipeline.setStages(stages);
        return pipeline;
    }

    private StageDefinition createStage(String name, String type) {
        return createStage(name, type, 0);
    }

    private StageDefinition createStage(String name, String type, int retry) {
        StageDefinition stage = new StageDefinition();
        stage.setName(name);
        stage.setType(type);
        stage.setRetry(retry);
        return stage;
    }
}
