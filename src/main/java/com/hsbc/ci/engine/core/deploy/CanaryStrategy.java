package com.hsbc.ci.engine.core.deploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CanaryStrategy implements DeploymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(CanaryStrategy.class);

    @Override
    public String deploy(String image, String target, Map<String, Object> config) {
        String namespace = (String) config.getOrDefault("namespace", "default");
        int trafficPercentage = (int) config.getOrDefault("trafficPercentage", 10);
        String canaryName = (String) config.getOrDefault("canaryDeployment", "app-canary");
        
        log.info("Deploying with Canary strategy: image={}, target={}, traffic={}%", 
            image, target, trafficPercentage);
        
        if (trafficPercentage < 0 || trafficPercentage > 100) {
            return "Canary deployment failed: traffic percentage must be 0-100";
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("kubectl", "create", "deployment", canaryName, 
                "--image=" + image, "-n", namespace, "--dry-run=client", "-o", "yaml");
            
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                return "Canary deployment: failed to create canary deployment";
            }
            
            pb = new ProcessBuilder();
            pb.command("kubectl", "expose", "deployment", canaryName, 
                "--name=" + canaryName + "-svc", "--type=ClusterIP", "-n", namespace,
                "--dry-run=client", "-o", "yaml");
            
            pb.redirectErrorStream(true);
            p = pb.start();
            p.waitFor();
            
            log.info("Canary deployment prepared with {}% traffic", trafficPercentage);
            return "Canary deployment prepared: canary deployment=" + canaryName + 
                   ", traffic=" + trafficPercentage + "%. Monitor metrics before increasing traffic.";
        } catch (Exception e) {
            log.error("Canary deployment failed: {}", e.getMessage());
            return "Canary deployment failed: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "Canary";
    }
}
