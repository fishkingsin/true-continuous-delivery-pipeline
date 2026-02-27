package com.cdengine.stages;

import org.springframework.stereotype.Component;
import com.cdengine.model.PipelineContext;
import java.util.Map;

@Component
public class ContainerizeStage implements Stage {

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String dockerfile = (String) config.getOrDefault("dockerfile", "Dockerfile");
        
        String image = "myapp";
        String tag = context.getVariable("GIT_COMMIT");
        if (tag == null) tag = "latest";
        
        System.out.println("  Building Docker image: " + image + ":" + tag);
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("docker", "build", "-t", image + ":" + tag, "-f", dockerfile, ".");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Docker build failed with exit code: " + exitCode);
            }
            
            return "Containerized successfully: " + image + ":" + tag;
        } catch (Exception e) {
            throw new RuntimeException("Containerization failed: " + e.getMessage());
        }
    }
}
