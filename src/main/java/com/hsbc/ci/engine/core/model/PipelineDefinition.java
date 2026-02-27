package com.hsbc.ci.engine.core.model;

import java.util.List;
import java.util.Map;

public class PipelineDefinition {

    private String name;
    private String version;
    private String description;
    private List<StageDefinition> stages;
    private List<String> environments;
    private Map<String, String> variables;
    private Integer timeout;

    public PipelineDefinition() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StageDefinition> getStages() {
        return stages;
    }

    public void setStages(List<StageDefinition> stages) {
        this.stages = stages;
    }

    public List<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<String> environments) {
        this.environments = environments;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
