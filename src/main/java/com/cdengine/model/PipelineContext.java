package com.cdengine.model;

import java.util.HashMap;
import java.util.Map;

public class PipelineContext {
    private final String pipelineName;
    private final String environment;
    private final boolean dryRun;
    private final Map<String, String> variables = new HashMap<>();
    private final Map<String, StageResult> stageResults = new HashMap<>();

    private PipelineContext(Builder builder) {
        this.pipelineName = builder.pipelineName;
        this.environment = builder.environment;
        this.dryRun = builder.dryRun;
        this.variables.putAll(builder.variables);
    }

    public String getPipelineName() { return pipelineName; }
    public String getEnvironment() { return environment; }
    public boolean isDryRun() { return dryRun; }
    public Map<String, String> getVariables() { return variables; }
    public Map<String, StageResult> getStageResults() { return stageResults; }
    
    public void addVariable(String key, String value) {
        variables.put(key, value);
    }
    
    public void addStageResult(String stageName, StageResult result) {
        stageResults.put(stageName, result);
    }
    
    public String getVariable(String key) {
        return variables.get(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pipelineName;
        private String environment;
        private boolean dryRun;
        private Map<String, String> variables = new HashMap<>();

        public Builder pipelineName(String pipelineName) { this.pipelineName = pipelineName; return this; }
        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder dryRun(boolean dryRun) { this.dryRun = dryRun; return this; }
        public Builder variables(Map<String, String> variables) { this.variables = variables; return this; }

        public PipelineContext build() {
            return new PipelineContext(this);
        }
    }
}
