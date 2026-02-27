package com.hsbc.ci.engine.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.hsbc.ci.engine.core.model.PipelineDefinition;
import com.hsbc.ci.engine.core.model.StageDefinition;
import com.hsbc.ci.engine.core.model.Environment;
import com.hsbc.ci.engine.core.model.Gate;

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

    @SuppressWarnings("unchecked")
    public PipelineDefinition loadPipelineDefinition(String name) {
        Map<String, Object> config = getPipeline(name);
        if (config == null) {
            return null;
        }

        PipelineDefinition pipeline = new PipelineDefinition();
        pipeline.setName((String) config.get("name"));
        pipeline.setVersion((String) config.get("version"));
        pipeline.setDescription((String) config.get("description"));
        pipeline.setTimeout((Integer) config.get("timeout"));

        List<String> envs = (List<String>) config.get("environments");
        if (envs != null) {
            pipeline.setEnvironments(envs);
        }

        Map<String, String> vars = (Map<String, String>) config.get("variables");
        if (vars != null) {
            pipeline.setVariables(vars);
        }

        List<Map<String, Object>> stagesData = (List<Map<String, Object>>) config.get("stages");
        if (stagesData != null) {
            List<StageDefinition> stages = new ArrayList<>();
            for (Map<String, Object> stageData : stagesData) {
                StageDefinition stage = new StageDefinition();
                stage.setName((String) stageData.get("name"));
                stage.setType((String) stageData.get("type"));
                stage.setEnabled((Boolean) stageData.getOrDefault("enabled", true));
                stage.setRetry((Integer) stageData.get("retry"));
                stage.setTimeout((Integer) stageData.get("timeout"));
                stage.setEnvironment((String) stageData.get("environment"));
                stage.setTarget((String) stageData.get("target"));
                stage.setAutoPromote((Boolean) stageData.get("auto-promote"));

                List<String> deps = (List<String>) stageData.get("depends-on");
                if (deps != null) {
                    stage.setDependsOn(deps);
                }

                Map<String, Object> stageConfig = (Map<String, Object>) stageData.get("config");
                if (stageConfig != null) {
                    stage.setConfig(stageConfig);
                }

                List<Map<String, Object>> gatesData = (List<Map<String, Object>>) stageData.get("gates");
                if (gatesData != null) {
                    List<Gate> gates = new ArrayList<>();
                    for (Map<String, Object> gateData : gatesData) {
                        Gate gate = new Gate();
                        Object typeObj = gateData.get("type");
                        if (typeObj instanceof String) {
                            gate.setType((String) typeObj);
                        } else if (typeObj instanceof Map) {
                            Map<String, Object> typeMap = (Map<String, Object>) typeObj;
                            gate.setType((String) typeMap.get("type"));
                            gate.setMaxCritical((Integer) typeMap.get("max-critical"));
                            gate.setMaxHigh((Integer) typeMap.get("max-high"));
                        }
                        gates.add(gate);
                    }
                    stage.setGates(gates);
                }

                stages.add(stage);
            }
            pipeline.setStages(stages);
        }

        return pipeline;
    }
}
