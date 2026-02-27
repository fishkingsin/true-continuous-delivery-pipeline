package com.hsbc.ci.engine.core.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BlueGreenStrategy implements DeploymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(BlueGreenStrategy.class);

    @Override
    public String deploy(String image, String target, Map<String, Object> config) {
        String namespace = (String) config.getOrDefault("namespace", "default");
        String serviceName = (String) config.getOrDefault("service", "app-service");
        String greenName = (String) config.getOrDefault("greenDeployment", "app-green");
        
        log.info("Deploying with Blue-Green strategy: image={}, target={}", image, target);
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("kubectl", "create", "deployment", greenName, 
                "--image=" + image, "-n", namespace, "--dry-run=client", "-o", "yaml");
            
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                return "Blue-Green deployment: failed to create green deployment";
            }
            
            pb = new ProcessBuilder();
            pb.command("kubectl", "expose", "deployment", greenName, 
                "--name=" + serviceName + "-new", "--type=ClusterIP", "-n", namespace,
                "--dry-run=client", "-o", "yaml");
            
            pb.redirectErrorStream(true);
            p = pb.start();
            p.waitFor();
            
            log.info("Blue-Green deployment prepared for {}", greenName);
            return "Blue-Green deployment prepared: green deployment=" + greenName + 
                   ". Switch traffic after verifying the new deployment.";
        } catch (Exception e) {
            log.error("Blue-Green deployment failed: {}", e.getMessage());
            return "Blue-Green deployment failed: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "Blue-Green";
    }
}
