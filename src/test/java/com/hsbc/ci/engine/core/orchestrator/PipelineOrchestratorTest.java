package com.hsbc.ci.engine.core.orchestrator;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.stages.StageExecutor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PipelineOrchestratorTest {

    @Test
    void orchestrator_hasPluginManagerField() throws Exception {
        PipelineOrchestrator orchestrator = new PipelineOrchestrator();
        
        boolean hasPluginManager = false;
        for (Field field : PipelineOrchestrator.class.getDeclaredFields()) {
            if (field.getType().equals(PluginManager.class)) {
                hasPluginManager = true;
                break;
            }
        }
        
        assertTrue(hasPluginManager, "PipelineOrchestrator should have PluginManager field");
    }

    @Test
    void orchestrator_canBeConstructedWithDependencies() {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        StageExecutor stageExecutor = new StageExecutor();
        PluginManager pluginManager = new PluginManager();
        
        PipelineOrchestrator orchestrator = new PipelineOrchestrator(configLoader, stageExecutor, pluginManager);
        
        assertNotNull(orchestrator);
    }
}
