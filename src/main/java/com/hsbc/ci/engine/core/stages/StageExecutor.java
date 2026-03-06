package com.hsbc.ci.engine.core.stages;

import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.StageResult;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.plugin.StagePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StageExecutor {

    private static final Logger log = LoggerFactory.getLogger(StageExecutor.class);

    private static final String STAGE_TYPE_BUILD = "build";
    private static final String STAGE_TYPE_TEST = "test";
    private static final String STAGE_TYPE_CONTAINERIZE = "containerize";
    private static final String STAGE_TYPE_DEPLOY = "deploy";
    private static final String STAGE_TYPE_PLUGIN_PREFIX = "plugin:";

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

    public StageResult execute(String stageType, Map<String, Object> config, PipelineContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            Stage stage = resolveStage(stageType);
            String output = executeStage(stage, stageType, config, context);
            return buildSuccessResult(stageType, output, startTime);
            
        } catch (Exception e) {
            log.error("Stage '{}' failed: {}", stageType, e.getMessage());
            return buildFailureResult(stageType, e.getMessage(), startTime);
        }
    }

    private Stage resolveStage(String stageType) {
        if (stageType == null) {
            return null;
        }

        if (isPluginStage(stageType)) {
            return resolvePluginStage(stageType);
        }

        return resolveBuiltInStage(stageType);
    }

    private boolean isPluginStage(String stageType) {
        return stageType.startsWith(STAGE_TYPE_PLUGIN_PREFIX);
    }

    private Stage resolveBuiltInStage(String type) {
        return switch (type) {
            case STAGE_TYPE_BUILD -> buildStage;
            case STAGE_TYPE_TEST -> testStage;
            case STAGE_TYPE_CONTAINERIZE -> containerizeStage;
            case STAGE_TYPE_DEPLOY -> deployStage;
            default -> null;
        };
    }

    private Stage resolvePluginStage(String pluginType) {
        String pluginName = extractPluginName(pluginType);
        StagePlugin plugin = pluginManager.getStagePlugin(pluginName);
        
        if (plugin == null) {
            log.warn("Plugin not found: {}", pluginName);
            return null;
        }
        
        return new PluginStageWrapper(pluginName, plugin);
    }

    private String extractPluginName(String pluginType) {
        return pluginType.substring(STAGE_TYPE_PLUGIN_PREFIX.length());
    }

    private String executeStage(Stage stage, String stageType, Map<String, Object> config, PipelineContext context) {
        if (stage == null) {
            throw new IllegalArgumentException("Unknown stage type: " + stageType);
        }
        
        log.info("Executing stage: {}", stageType);
        return stage.execute(config, context);
    }

    private StageResult buildSuccessResult(String stageType, String output, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("Stage '{}' completed in {}ms", stageType, duration);
        
        return StageResult.builder()
            .stageName(stageType)
            .success(true)
            .output(output)
            .durationMs(duration)
            .build();
    }

    private StageResult buildFailureResult(String stageType, String errorMessage, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        
        return StageResult.builder()
            .stageName(stageType)
            .success(false)
            .output("Error: " + errorMessage)
            .durationMs(duration)
            .build();
    }
}
