package com.hsbc.ci.engine.core.plugin;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PluginManager {

    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

    private String pluginPath = "plugins";
    private final Map<String, StagePlugin> stagePlugins = new ConcurrentHashMap<>();
    private final Map<String, GatePlugin> gatePlugins = new ConcurrentHashMap<>();
    private final Map<String, NotifierPlugin> notifierPlugins = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadPlugins();
    }

    public void setPluginPath(String path) {
        this.pluginPath = path;
        loadPlugins();
    }

    private void loadPlugins() {
        Path pluginDir = Paths.get(pluginPath);
        if (!Files.exists(pluginDir)) {
            log.info("Plugin directory not found: {}", pluginDir);
            return;
        }

        try {
            loadPluginConfig();
        } catch (Exception e) {
            log.warn("Failed to load plugin config: {}", e.getMessage());
        }

        log.info("Loaded {} stage plugins, {} gate plugins, {} notifier plugins",
            stagePlugins.size(), gatePlugins.size(), notifierPlugins.size());
    }

    @SuppressWarnings("unchecked")
    private void loadPluginConfig() throws Exception {
        Path configFile = Paths.get("config/plugins.yml");
        if (!Files.exists(configFile)) {
            log.info("No plugins.yml found, using built-in plugins only");
            loadBuiltInPlugins();
            return;
        }

        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(Files.readString(configFile));
        Map<String, Object> plugins = (Map<String, Object>) config.get("plugins");

        if (plugins == null) {
            loadBuiltInPlugins();
            return;
        }

        for (Map.Entry<String, Object> entry : plugins.entrySet()) {
            Map<String, Object> pluginConfig = (Map<String, Object>) entry.getValue();
            Boolean enabled = (Boolean) pluginConfig.getOrDefault("enabled", true);
            
            if (!Boolean.TRUE.equals(enabled)) {
                log.info("Plugin disabled: {}", entry.getKey());
                continue;
            }

            log.info("Registering plugin: {}", entry.getKey());
        }
    }

    private void loadBuiltInPlugins() {
        log.info("Using built-in plugins only");
    }

    public void registerStage(StagePlugin plugin) {
        stagePlugins.put(plugin.getName(), plugin);
        log.info("Registered stage plugin: {}", plugin.getName());
    }

    public void registerGate(GatePlugin plugin) {
        gatePlugins.put(plugin.getName(), plugin);
        log.info("Registered gate plugin: {}", plugin.getName());
    }

    public void registerNotifier(NotifierPlugin plugin) {
        notifierPlugins.put(plugin.getName(), plugin);
        log.info("Registered notifier plugin: {}", plugin.getName());
    }

    public StagePlugin getStagePlugin(String name) {
        return stagePlugins.get(name);
    }

    public GatePlugin getGatePlugin(String name) {
        return gatePlugins.get(name);
    }

    public NotifierPlugin getNotifierPlugin(String name) {
        return notifierPlugins.get(name);
    }

    public Collection<String> listStagePlugins() {
        return stagePlugins.keySet();
    }

    public Collection<String> listGatePlugins() {
        return gatePlugins.keySet();
    }

    public Collection<String> listNotifierPlugins() {
        return notifierPlugins.keySet();
    }

    public Map<String, Collection<String>> listAllPlugins() {
        Map<String, Collection<String>> result = new HashMap<>();
        result.put("stages", listStagePlugins());
        result.put("gates", listGatePlugins());
        result.put("notifiers", listNotifierPlugins());
        return result;
    }
}
