package com.hsbc.ci.engine.core.config;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Component
public class ConfigurationLoader {

    private String configPath = "config";
    private Map<String, Object> pipelineConfigs = new HashMap<>();

    @PostConstruct
    public void init() {
        loadPipelines();
    }

    public void setConfigPath(String path) {
        this.configPath = path;
        loadPipelines();
    }

    private void loadPipelines() {
        Path pipelineDir = Paths.get(configPath, "pipelines");
        if (!Files.exists(pipelineDir)) {
            System.out.println("[WARN] Pipeline directory not found: " + pipelineDir);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pipelineDir, "*.yml")) {
            for (Path file : stream) {
                String name = file.getFileName().toString().replace(".yml", "");
                Yaml yaml = new Yaml();
                pipelineConfigs.put(name, yaml.load(Files.readString(file)));
            }
            System.out.println("[INFO] Loaded " + pipelineConfigs.size() + " pipelines");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load pipelines: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPipeline(String name) {
        return (Map<String, Object>) pipelineConfigs.get(name);
    }

    public Collection<String> listPipelines() {
        return pipelineConfigs.keySet();
    }
}
