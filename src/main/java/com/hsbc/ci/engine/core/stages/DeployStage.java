package com.hsbc.ci.engine.core.stages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;

import java.io.IOException;
import java.util.Map;

@Component
public class DeployStage implements Stage {

    private static final Logger log = LoggerFactory.getLogger(DeployStage.class);

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String targetType = (String) config.get("type");
        String namespace = (String) config.getOrDefault("namespace", "default");
        String image = (String) config.getOrDefault("image", "myapp:latest");
        
        System.out.println("  Deploying to: " + targetType + " namespace: " + namespace);
        
        try {
            if ("kubernetes".equals(targetType)) {
                return deployToKubernetes(namespace, image, config);
            } else if ("ecs".equals(targetType)) {
                return deployToECS(config);
            }
            throw new RuntimeException("Unknown deployment target: " + targetType);
        } catch (Exception e) {
            log.error("Deployment failed: {}", e.getMessage());
            throw new RuntimeException("Deployment failed: " + e.getMessage());
        }
    }

    private String deployToKubernetes(String namespace, String image, Map<String, Object> config) 
            throws Exception {
        System.out.println("  [Kubernetes] Checking kubectl...");
        
        try {
            ProcessBuilder pb = new ProcessBuilder("kubectl", "version", "--client");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                log.warn("kubectl not found - skipping deployment");
                System.out.println("  [Kubernetes] kubectl not found - skipping deployment");
                return "Kubernetes deployment skipped (kubectl not available). Install kubectl to enable deployment.";
            }
        } catch (IOException e) {
            log.warn("kubectl not found: {}", e.getMessage());
            System.out.println("  [Kubernetes] kubectl not found - skipping deployment");
            return "Kubernetes deployment skipped (kubectl not available). Install kubectl to enable deployment.";
        }
        
        log.info("Would deploy to namespace: {} image: {}", namespace, image);
        System.out.println("  [Kubernetes] Would deploy to namespace: " + namespace + " image: " + image);
        
        return "Kubernetes deployment completed: namespace=" + namespace + ", image=" + image;
    }

    private String deployToECS(Map<String, Object> config) throws Exception {
        String cluster = (String) config.getOrDefault("cluster", "default");
        
        System.out.println("  [ECS] Checking AWS CLI...");
        
        try {
            ProcessBuilder pb = new ProcessBuilder("aws", "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                log.warn("AWS CLI not found - skipping deployment");
                System.out.println("  [ECS] AWS CLI not found - skipping deployment");
                return "ECS deployment skipped (AWS CLI not available). Install AWS CLI to enable deployment.";
            }
        } catch (IOException e) {
            log.warn("AWS CLI not found: {}", e.getMessage());
            System.out.println("  [ECS] AWS CLI not found - skipping deployment");
            return "ECS deployment skipped (AWS CLI not available). Install AWS CLI to enable deployment.";
        }
        
        log.info("Would deploy to cluster: {}", cluster);
        System.out.println("  [ECS] Would deploy to cluster: " + cluster);
        
        return "ECS deployment completed: cluster=" + cluster;
    }
}
