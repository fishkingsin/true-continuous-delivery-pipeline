package com.hsbc.ci.engine.core.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.PipelineResult;
import com.hsbc.ci.engine.core.model.StageResult;
import com.hsbc.ci.engine.core.plugin.GatePlugin;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.plugin.StagePlugin;
import com.hsbc.ci.engine.core.stages.StageExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PipelineOrchestrator {

    private ConfigurationLoader configLoader;
    private StageExecutor stageExecutor;
    private PluginManager pluginManager;

    public PipelineOrchestrator() {
    }

    @Autowired
    public PipelineOrchestrator(ConfigurationLoader configLoader, 
                                StageExecutor stageExecutor,
                                PluginManager pluginManager) {
        this.configLoader = configLoader;
        this.stageExecutor = stageExecutor;
        this.pluginManager = pluginManager;
    }

    public PipelineResult execute(PipelineContext context) {
        String pipelineName = context.getPipelineName();
        Map<String, Object> pipelineConfig = configLoader.getPipeline(pipelineName);

        if (pipelineConfig == null) {
            return PipelineResult.failed("Pipeline not found: " + pipelineName);
        }

        if (context.isDryRun()) {
            System.out.println("[DRY-RUN] Would execute pipeline: " + pipelineName);
            return PipelineResult.success(context);
        }

        System.out.println("[INFO] Executing pipeline: " + pipelineName);

        executePreStagePlugins(context);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stages = (List<Map<String, Object>>) pipelineConfig.get("stages");

        if (stages == null) {
            return PipelineResult.failed("No stages defined in pipeline");
        }

        for (Map<String, Object> stageConfig : stages) {
            String stageName = (String) stageConfig.get("name");
            String stageType = (String) stageConfig.get("type");

            System.out.println("[INFO] Executing stage: " + stageName);

            executeStagePlugins(stageName, context);

            StageResult result = stageExecutor.execute(stageType, stageConfig, context);
            context.addStageResult(stageName, result);

            if (!result.isSuccess()) {
                return PipelineResult.failed("Stage failed: " + stageName);
            }
        }

        executePostStagePlugins(context);

        return PipelineResult.success(context);
    }

    private void executePreStagePlugins(PipelineContext context) {
        var plugins = pluginManager.listStagePlugins();
        for (String pluginName : plugins) {
            StagePlugin plugin = pluginManager.getStagePlugin(pluginName);
            if (plugin != null) {
                System.out.println("[INFO] Running pre-stage plugin: " + pluginName);
                plugin.execute(new HashMap<>(), new HashMap<>());
            }
        }
    }

    private void executeStagePlugins(String stageName, PipelineContext context) {
        System.out.println("[DEBUG] Stage plugins would run here for: " + stageName);
    }

    private void executePostStagePlugins(PipelineContext context) {
        var plugins = pluginManager.listStagePlugins();
        for (String pluginName : plugins) {
            StagePlugin plugin = pluginManager.getStagePlugin(pluginName);
            if (plugin != null) {
                System.out.println("[INFO] Running post-stage plugin: " + pluginName);
                plugin.execute(new HashMap<>(), new HashMap<>());
            }
        }
    }
}
