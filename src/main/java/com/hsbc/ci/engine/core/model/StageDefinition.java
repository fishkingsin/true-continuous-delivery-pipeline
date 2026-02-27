package com.hsbc.ci.engine.core.model;

import java.util.List;
import java.util.Map;

public class StageDefinition {

    private String name;
    private String type;
    private Map<String, Object> config;
    private List<String> dependsOn;
    private Boolean enabled;
    private Integer retry;
    private Integer timeout;
    private String environment;
    private String target;
    private List<Gate> gates;
    private Boolean autoPromote;

    public StageDefinition() {
        this.enabled = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public void setGates(List<Gate> gates) {
        this.gates = gates;
    }

    public Boolean getAutoPromote() {
        return autoPromote;
    }

    public void setAutoPromote(Boolean autoPromote) {
        this.autoPromote = autoPromote;
    }
}
