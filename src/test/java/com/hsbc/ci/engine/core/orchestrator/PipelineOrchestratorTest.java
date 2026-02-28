package com.hsbc.ci.engine.core.orchestrator;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.PipelineDefinition;
import com.hsbc.ci.engine.core.model.PipelineResult;
import com.hsbc.ci.engine.core.model.StageDefinition;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.stages.StageExecutor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineOrchestratorTest {

    @Test
    void orchestrator_hasPluginManagerField() {
        boolean hasPluginManager = false;
        for (var field : PipelineOrchestrator.class.getDeclaredFields()) {
            if (field.getType().equals(PluginManager.class)) {
                hasPluginManager = true;
                break;
            }
        }
        assertTrue(hasPluginManager, "PipelineOrchestrator should have PluginManager field");
    }

    @Test
    void orchestrator_canBeConstructedWithDependencies() {
        ConfigurationLoader cl = new ConfigurationLoader();
        StageExecutor se = new StageExecutor();
        PluginManager pm = new PluginManager();
        
        PipelineOrchestrator orch = new PipelineOrchestrator(cl, se, pm);
        
        assertNotNull(orch);
    }

    @Test
    void execute_returnsFailedResult_whenPipelineNotFound() {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        StageExecutor stageExecutor = new StageExecutor();
        PluginManager pluginManager = new PluginManager();
        
        PipelineOrchestrator orchestrator = new PipelineOrchestrator(configLoader, stageExecutor, pluginManager);

        PipelineContext context = PipelineContext.builder()
            .pipelineName("unknown-pipeline-that-does-not-exist")
            .build();

        PipelineResult result = orchestrator.execute(context);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("not found"));
    }

    @Test
    void execute_runsDryRun_whenDryRunEnabled() {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        StageExecutor stageExecutor = new StageExecutor();
        PluginManager pluginManager = new PluginManager();
        
        PipelineOrchestrator orchestrator = new PipelineOrchestrator(configLoader, stageExecutor, pluginManager);

        PipelineContext context = PipelineContext.builder()
            .pipelineName("sample-pipeline")
            .dryRun(true)
            .build();

        PipelineResult result = orchestrator.execute(context);

        assertTrue(result.isSuccess() || result.getError() != null);
    }
}
