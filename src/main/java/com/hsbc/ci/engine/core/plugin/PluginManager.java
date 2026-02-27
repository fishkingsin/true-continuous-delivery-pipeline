package com.hsbc.ci.engine.core.plugin;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PluginManager {

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
            System.out.println("[INFO] Plugin directory not found: " + pluginDir);
            return;
        }

        try {
            loadPluginConfig();
        } catch (Exception e) {
            System.err.println("[WARN] Failed to load plugin config: " + e.getMessage());
        }

        System.out.println("[INFO] Loaded " + stagePlugins.size() + " stage plugins");
        System.out.println("[INFO] Loaded " + gatePlugins.size() + " gate plugins");
        System.out.println("[INFO] Loaded " + notifierPlugins.size() + " notifier plugins");
    }

    @SuppressWarnings("unchecked")
    private void loadPluginConfig() throws Exception {
        Path configFile = Paths.get("config/plugins.yml");
        if (!Files.exists(configFile)) {
            System.out.println("[INFO] No plugins.yml found, using built-in plugins only");
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
                System.out.println("[INFO] Plugin disabled: " + entry.getKey());
                continue;
            }

            System.out.println("[INFO] Registering plugin: " + entry.getKey());
        }
    }

    private void loadBuiltInPlugins() {
        System.out.println("[INFO] Using built-in plugins only");
    }

    public void registerStage(StagePlugin plugin) {
        stagePlugins.put(plugin.getName(), plugin);
        System.out.println("[INFO] Registered stage plugin: " + plugin.getName());
    }

    public void registerGate(GatePlugin plugin) {
        gatePlugins.put(plugin.getName(), plugin);
        System.out.println("[INFO] Registered gate plugin: " + plugin.getName());
    }

    public void registerNotifier(NotifierPlugin plugin) {
        notifierPlugins.put(plugin.getName(), plugin);
        System.out.println("[INFO] Registered notifier plugin: " + plugin.getName());
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
