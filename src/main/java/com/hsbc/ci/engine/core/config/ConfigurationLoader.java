package com.hsbc.ci.engine.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Component
public class ConfigurationLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationLoader.class);

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
            log.warn("Pipeline directory not found: {}", pipelineDir);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pipelineDir, "*.yml")) {
            for (Path file : stream) {
                String name = file.getFileName().toString().replace(".yml", "");
                Yaml yaml = new Yaml();
                pipelineConfigs.put(name, yaml.load(Files.readString(file)));
            }
            log.info("Loaded {} pipelines", pipelineConfigs.size());
        } catch (IOException e) {
            log.error("Failed to load pipelines: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPipeline(String name) {
        return (Map<String, Object>) pipelineConfigs.get(name);
    }

    public Collection<String> listPipelines() {
        return pipelineConfigs.keySet();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadYamlFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new FileNotFoundException("Config file not found: " + filePath);
            }
            Yaml yaml = new Yaml();
            return yaml.load(Files.readString(path));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + e.getMessage(), e);
        }
    }
}
