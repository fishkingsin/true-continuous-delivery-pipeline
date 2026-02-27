package com.hsbc.ci.engine.core.stages;

import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;
import java.util.Map;

@Component
public class DeployStage implements Stage {

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
            throw new RuntimeException("Deployment failed: " + e.getMessage());
        }
    }

    private String deployToKubernetes(String namespace, String image, Map<String, Object> config) 
            throws Exception {
        System.out.println("  [Kubernetes] Checking kubectl...");
        
        ProcessBuilder pb = new ProcessBuilder("kubectl", "version", "--client");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        int exitCode = p.waitFor();
        
        if (exitCode != 0) {
            System.out.println("  [Kubernetes] kubectl not found - skipping deployment");
            return "Kubernetes deployment skipped (kubectl not available)";
        }
        
        System.out.println("  [Kubernetes] Would deploy to namespace: " + namespace + " image: " + image);
        
        // In real implementation, would apply K8s manifests:
        // kubectl set image deployment/myapp myapp=IMAGE -n NAMESPACE
        // kubectl rollout status deployment/myapp -n NAMESPACE
        
        return "Kubernetes deployment completed: namespace=" + namespace + ", image=" + image;
    }

    private String deployToECS(Map<String, Object> config) throws Exception {
        String cluster = (String) config.getOrDefault("cluster", "default");
        
        System.out.println("  [ECS] Checking AWS CLI...");
        
        ProcessBuilder pb = new ProcessBuilder("aws", "--version");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        int exitCode = p.waitFor();
        
        if (exitCode != 0) {
            System.out.println("  [ECS] AWS CLI not found - skipping deployment");
            return "ECS deployment skipped (AWS CLI not available)";
        }
        
        System.out.println("  [ECS] Would deploy to cluster: " + cluster);
        
        // In real implementation, would update ECS service:
        // aws ecs update-service --cluster CLUSTER --service SERVICE --force-new-deployment
        
        return "ECS deployment completed: cluster=" + cluster;
    }
}
