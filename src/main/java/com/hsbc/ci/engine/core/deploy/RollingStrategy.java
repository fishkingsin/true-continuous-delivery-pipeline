package com.hsbc.ci.engine.core.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RollingStrategy implements DeploymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(RollingStrategy.class);

    @Override
    public String deploy(String image, String target, Map<String, Object> config) {
        String namespace = (String) config.getOrDefault("namespace", "default");
        int maxSurge = (int) config.getOrDefault("maxSurge", 1);
        int maxUnavailable = (int) config.getOrDefault("maxUnavailable", 0);
        
        log.info("Deploying with RollingUpdate strategy: image={}, target={}, maxSurge={}, maxUnavailable={}", 
            image, target, maxSurge, maxUnavailable);
        
        String deploymentName = (String) config.getOrDefault("deployment", "app");
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("kubectl", "set", "image", "deployment/" + deploymentName, 
                deploymentName + "=" + image, 
                "-n", namespace);
            
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                log.error("Rolling update failed");
                return "RollingUpdate deployment failed";
            }
            
            pb = new ProcessBuilder();
            pb.command("kubectl", "rollout", "status", "deployment/" + deploymentName, 
                "-n", namespace, "--timeout=300s");
            
            pb.redirectErrorStream(true);
            p = pb.start();
            exitCode = p.waitFor();
            
            if (exitCode == 0) {
                log.info("RollingUpdate completed successfully");
                return "RollingUpdate deployment completed successfully";
            } else {
                return "RollingUpdate deployment timed out";
            }
            
        } catch (Exception e) {
            log.error("RollingUpdate failed: {}", e.getMessage());
            return "RollingUpdate deployment failed: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "RollingUpdate";
    }
}
