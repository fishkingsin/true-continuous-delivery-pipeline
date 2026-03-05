package com.hsbc.ci.engine.core.stages;

import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.plugin.StagePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PluginStageWrapper implements Stage {

    private static final Logger log = LoggerFactory.getLogger(PluginStageWrapper.class);
    
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
            log.info("Plugin executed successfully: {}", pluginName);
            return "[SUCCESS] Plugin executed: " + pluginName;
        } catch (Exception e) {
            log.error("Plugin execution failed: {} - {}", pluginName, e.getMessage());
            return "[ERROR] Plugin execution failed: " + pluginName + " - " + e.getMessage();
        }
    }
}
