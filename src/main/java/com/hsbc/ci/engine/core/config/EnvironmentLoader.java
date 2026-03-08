package com.hsbc.ci.engine.core.config;

import com.hsbc.ci.engine.core.model.Environment;
import com.hsbc.ci.engine.core.model.PipelineContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EnvironmentLoader {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentLoader.class);

    private String configPath = "config";
    private Map<String, Environment> environments = new HashMap<>();

    public Map<String, Environment> getEnvironments() {
        return environments;
    }

    @PostConstruct
    public void init() {
        loadEnvironments();
    }

    public void setConfigPath(String path) {
        this.configPath = path;
        loadEnvironments();
    }

    private void loadEnvironments() {
        Path envDir = Paths.get(configPath, "environments");
        
        if (Files.exists(Paths.get(configPath, "environments.yml"))) {
            loadFromEnvironmentsYaml(Paths.get(configPath, "environments.yml"));
        } else if (Files.exists(envDir)) {
            loadFromDirectory(envDir);
        } else {
            log.warn("No environment configuration found");
        }
        
        log.info("Loaded {} environments", environments.size());
    }

    private void loadFromEnvironmentsYaml(Path file) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(Files.readString(file));
            
            Object envsObj = data.get("environments");
            if (envsObj instanceof List) {
                List<Map<String, Object>> envs = (List<Map<String, Object>>) envsObj;
                for (Map<String, Object> envData : envs) {
                    Environment env = parseEnvironment(envData);
                    environments.put(env.getName(), env);
                }
            }
        } catch (Exception e) {
            log.error("Failed to load environments from {}: {}", file, e.getMessage());
        }
    }

    private void loadFromDirectory(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.yml")) {
            for (Path file : stream) {
                try {
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(Files.readString(file));
                    Environment env = parseEnvironment(data);
                    environments.put(env.getName(), env);
                } catch (Exception e) {
                    log.error("Failed to load environment from {}: {}", file, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Failed to read environment directory: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Environment parseEnvironment(Map<String, Object> data) {
        Environment env = new Environment();
        
        setBasicFields(env, data);
        env.setDeploy(parseDeploy(data.get("deploy")));
        env.setApproval(parseApproval(data.get("approval")));
        env.setResources(parseResources(data.get("resources")));
        env.setGates((List<String>) data.get("gates"));
        env.setMonitoring((Map<String, Boolean>) data.get("monitoring"));
        
        return env;
    }

    private void setBasicFields(Environment env, Map<String, Object> data) {
        env.setName((String) data.get("name"));
        env.setDescription((String) data.get("description"));
        env.setOrder((Integer) data.get("order"));
        env.setAutoPromote((Boolean) data.get("auto-promote"));
        env.setReplicas((Integer) data.get("replicas"));
        env.setBackup((Boolean) data.get("backup"));
    }

    private Environment.DeployConfig parseDeploy(Object data) {
        if (data == null) return null;
        var deploy = new Environment.DeployConfig();
        var d = (Map<String, Object>) data;
        deploy.setType((String) d.get("type"));
        deploy.setNamespace((String) d.get("namespace"));
        deploy.setCluster((String) d.get("cluster"));
        deploy.setStrategy((String) d.get("strategy"));
        return deploy;
    }

    private Environment.Approval parseApproval(Object data) {
        if (data == null) return null;
        var approval = new Environment.Approval();
        var a = (Map<String, Object>) data;
        approval.setType((String) a.get("type"));
        approval.setTimeout((String) a.get("timeout"));
        approval.setRoles((List<String>) a.get("roles"));
        return approval;
    }

    private Environment.Resources parseResources(Object data) {
        if (data == null) return null;
        var resources = new Environment.Resources();
        var r = (Map<String, Object>) data;
        resources.setCpu((String) r.get("cpu"));
        resources.setMemory((String) r.get("memory"));
        return resources;
    }

    public Environment getEnvironment(String name) {
        return environments.get(name);
    }

    public Collection<Environment> listEnvironments() {
        return environments.values();
    }

    public List<Environment> getOrderedEnvironments() {
        return environments.values().stream()
            .sorted(Comparator.comparingInt(e -> e.getOrder() != null ? e.getOrder() : 999))
            .collect(Collectors.toList());
    }

    public Map<String, String> getEnvironmentVariables(String envName) {
        Environment env = getEnvironment(envName);
        if (env == null) {
            return Collections.emptyMap();
        }
        
        Map<String, String> vars = new HashMap<>();
        
        if (env.getResources() != null) {
            if (env.getResources().getCpu() != null) {
                vars.put("CPU", env.getResources().getCpu());
            }
            if (env.getResources().getMemory() != null) {
                vars.put("MEMORY", env.getResources().getMemory());
            }
        }
        
        if (env.getReplicas() != null) {
            vars.put("REPLICAS", env.getReplicas().toString());
        }
        
        if (env.getDeploy() != null) {
            if (env.getDeploy().getNamespace() != null) {
                vars.put("NAMESPACE", env.getDeploy().getNamespace());
            }
            if (env.getDeploy().getCluster() != null) {
                vars.put("CLUSTER", env.getDeploy().getCluster());
            }
        }
        
        return vars;
    }

    public boolean validate(String name) {
        Environment env = environments.get(name);
        if (env == null) {
            return false;
        }
        
        List<String> errors = new ArrayList<>();
        
        if (env.getName() == null || env.getName().isBlank()) {
            errors.add("Environment name is required");
        }
        
        if (env.getDeploy() != null) {
            if (env.getDeploy().getType() == null) {
                errors.add("Deploy type is required for environment: " + name);
            }
        }
        
        return errors.isEmpty();
    }

    public void applyEnvironmentOverrides(PipelineContext context) {
        String envName = context.getEnvironment();
        if (envName == null || envName.isBlank()) {
            return;
        }
        
        Environment env = getEnvironment(envName);
        if (env == null) {
            log.warn("Environment not found: {}", envName);
            return;
        }
        
        Map<String, String> envVars = getEnvironmentVariables(envName);
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            context.addVariable(entry.getKey(), entry.getValue());
        }
        
        context.addVariable("ENVIRONMENT_NAME", envName);
        context.addVariable("ENVIRONMENT_ORDER", String.valueOf(env.getOrder()));
        
        if (env.getDeploy() != null) {
            context.addVariable("DEPLOY_TYPE", env.getDeploy().getType());
            if (env.getDeploy().getNamespace() != null) {
                context.addVariable("DEPLOY_NAMESPACE", env.getDeploy().getNamespace());
            }
            if (env.getDeploy().getCluster() != null) {
                context.addVariable("DEPLOY_CLUSTER", env.getDeploy().getCluster());
            }
            if (env.getDeploy().getStrategy() != null) {
                context.addVariable("DEPLOY_STRATEGY", env.getDeploy().getStrategy());
            }
        }
        
        if (env.getApproval() != null) {
            context.addVariable("APPROVAL_TYPE", env.getApproval().getType());
        }
        
        log.info("Applied environment overrides for: {}", envName);
    }

    public Environment getNextEnvironment(String currentEnvName) {
        List<Environment> ordered = getOrderedEnvironments();
        
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).getName().equals(currentEnvName)) {
                if (i + 1 < ordered.size()) {
                    return ordered.get(i + 1);
                }
                break;
            }
        }
        
        return null;
    }

    public boolean shouldAutoPromote(String envName) {
        Environment env = getEnvironment(envName);
        return env != null && Boolean.TRUE.equals(env.getAutoPromote());
    }

    public List<Environment> getPromotionChain(String fromEnv) {
        List<Environment> ordered = getOrderedEnvironments();
        List<Environment> chain = new ArrayList<>();
        
        boolean found = false;
        for (Environment env : ordered) {
            if (env.getName().equals(fromEnv)) {
                found = true;
            }
            if (found) {
                chain.add(env);
            }
        }
        
        return chain;
    }
}
