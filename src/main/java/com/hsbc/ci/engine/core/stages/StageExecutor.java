package com.hsbc.ci.engine.core.stages;

import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.StageResult;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.plugin.StagePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StageExecutor {

    private final Map<String, Stage> stages = new HashMap<>();

    @Autowired
    private BuildStage buildStage;

    @Autowired
    private TestStage testStage;

    @Autowired
    private ContainerizeStage containerizeStage;

    @Autowired
    private DeployStage deployStage;

    @Autowired
    private PluginManager pluginManager;

    public StageExecutor() {
    }

    public StageResult execute(String stageType, 
                              Map<String, Object> config, 
                              PipelineContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            Stage stage = getStage(stageType);
            if (stage == null) {
                return StageResult.builder()
                    .stageName(stageType)
                    .success(false)
                    .output("Unknown stage type: " + stageType)
                    .durationMs(0)
                    .build();
            }

            String output = stage.execute(config, context);
            long duration = System.currentTimeMillis() - startTime;

            return StageResult.builder()
                .stageName(stageType)
                .success(true)
                .output(output)
                .durationMs(duration)
                .build();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return StageResult.builder()
                .stageName(stageType)
                .success(false)
                .output("Error: " + e.getMessage())
                .durationMs(duration)
                .build();
        }
    }

    private Stage getStage(String type) {
        if (type == null) {
            return null;
        }

        if (type.startsWith("plugin:")) {
            return getPluginStage(type);
        }

        return switch (type) {
            case "build" -> buildStage;
            case "test" -> testStage;
            case "containerize" -> containerizeStage;
            case "deploy" -> deployStage;
            default -> null;
        };
    }

    private Stage getPluginStage(String pluginType) {
        String pluginName = pluginType.substring("plugin:".length());
        StagePlugin plugin = pluginManager.getStagePlugin(pluginName);
        
        if (plugin == null) {
            return null;
        }
        
        return new PluginStageWrapper(pluginName, plugin);
    }

    private static class PluginStageWrapper implements Stage {
        private final String pluginName;
        private final StagePlugin plugin;

        public PluginStageWrapper(String pluginName, StagePlugin plugin) {
            this.pluginName = pluginName;
            this.plugin = plugin;
        }

        @Override
        public String execute(Map<String, Object> config, PipelineContext context) {
            Map<String, Object> pluginContext = new HashMap<>();
            if (context != null) {
                pluginContext.put("pipelineName", context.getPipelineName());
                pluginContext.put("environment", context.getEnvironment());
                pluginContext.put("variables", context.getVariables());
            }
            
            try {
                plugin.execute(config, pluginContext);
                return "[SUCCESS] Plugin executed: " + pluginName;
            } catch (Exception e) {
                return "[ERROR] Plugin execution failed: " + pluginName + " - " + e.getMessage();
            }
        }
    }
}
