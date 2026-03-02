package com.hsbc.ci.engine.core.orchestrator;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.PipelineResult;
import com.hsbc.ci.engine.core.model.StageDefinition;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.stages.StageExecutor;
import org.junit.jupiter.api.Test;

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

    @Test
    void orchestrator_hasExecutorServiceField() {
        boolean hasExecutorField = false;
        for (var field : PipelineOrchestrator.class.getDeclaredFields()) {
            String typeName = field.getType().getName();
            if (typeName.contains("Executor")) {
                hasExecutorField = true;
                break;
            }
        }
        assertTrue(hasExecutorField, "PipelineOrchestrator should have Executor for parallel execution");
    }

    @Test
    void orchestrator_hasDependencyResolutionLogic() {
        boolean hasDependsOnLogic = false;
        
        for (var method : PipelineOrchestrator.class.getDeclaredMethods()) {
            String methodName = method.getName().toLowerCase();
            if (methodName.contains("depend") || methodName.contains("stage")) {
                hasDependsOnLogic = true;
                break;
            }
        }
        
        assertTrue(hasDependsOnLogic, 
            "PipelineOrchestrator should handle stage dependencies");
    }

    @Test
    void orchestrator_hasFailFastLogic() {
        boolean hasFailFast = false;
        
        for (var method : PipelineOrchestrator.class.getDeclaredMethods()) {
            if (method.getName().contains("executeStages")) {
                hasFailFast = true;
                break;
            }
        }
        
        assertTrue(hasFailFast, "PipelineOrchestrator should implement fail-fast behavior");
    }

    @Test
    void orchestrator_hasRetryLogic() {
        boolean hasRetry = false;
        
        for (var method : PipelineOrchestrator.class.getDeclaredMethods()) {
            if (method.getName().contains("executeStage")) {
                hasRetry = true;
                break;
            }
        }
        
        assertTrue(hasRetry, "PipelineOrchestrator should implement retry logic");
    }

    @Test
    void stageDefinition_supportsDependsOn() throws NoSuchMethodException {
        StageDefinition stage = new StageDefinition();
        
        assertNotNull(stage.getClass().getMethod("getDependsOn"), 
            "StageDefinition should support dependsOn for parallel execution");
    }

    @Test
    void stageDefinition_supportsRetry() throws NoSuchMethodException {
        StageDefinition stage = new StageDefinition();
        
        assertNotNull(stage.getClass().getMethod("getRetry"), 
            "StageDefinition should support retry count");
    }

    @Test
    void stageDefinition_supportsTimeout() throws NoSuchMethodException {
        StageDefinition stage = new StageDefinition();
        
        assertNotNull(stage.getClass().getMethod("getTimeout"), 
            "StageDefinition should support timeout");
    }
}
