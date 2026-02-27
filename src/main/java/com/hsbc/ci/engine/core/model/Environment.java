package com.hsbc.ci.engine.core.model;

import java.util.List;
import java.util.Map;

public class Environment {
    private String name;
    private String description;
    private Integer order;
    private Boolean autoPromote;
    private DeployConfig deploy;
    private Approval approval;
    private List<String> gates;
    private Resources resources;
    private Integer replicas;
    private Map<String, Boolean> monitoring;
    private Boolean backup;

    public Environment() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public Boolean getAutoPromote() { return autoPromote; }
    public void setAutoPromote(Boolean autoPromote) { this.autoPromote = autoPromote; }

    public DeployConfig getDeploy() { return deploy; }
    public void setDeploy(DeployConfig deploy) { this.deploy = deploy; }

    public Approval getApproval() { return approval; }
    public void setApproval(Approval approval) { this.approval = approval; }

    public List<String> getGates() { return gates; }
    public void setGates(List<String> gates) { this.gates = gates; }

    public Resources getResources() { return resources; }
    public void setResources(Resources resources) { this.resources = resources; }

    public Integer getReplicas() { return replicas; }
    public void setReplicas(Integer replicas) { this.replicas = replicas; }

    public Map<String, Boolean> getMonitoring() { return monitoring; }
    public void setMonitoring(Map<String, Boolean> monitoring) { this.monitoring = monitoring; }

    public Boolean getBackup() { return backup; }
    public void setBackup(Boolean backup) { this.backup = backup; }

    public static class DeployConfig {
        private String type;
        private String namespace;
        private String cluster;
        private String strategy;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }

        public String getCluster() { return cluster; }
        public void setCluster(String cluster) { this.cluster = cluster; }

        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
    }

    public static class Approval {
        private String type;
        private List<String> roles;
        private String timeout;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public String getTimeout() { return timeout; }
        public void setTimeout(String timeout) { this.timeout = timeout; }
    }

    public static class Resources {
        private String cpu;
        private String memory;

        public String getCpu() { return cpu; }
        public void setCpu(String cpu) { this.cpu = cpu; }

        public String getMemory() { return memory; }
        public void setMemory(String memory) { this.memory = memory; }
    }
}
