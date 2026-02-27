package com.hsbc.ci.engine.core.stages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;
import java.util.Map;

@Component
public class ContainerizeStage implements Stage {

    private static final Logger log = LoggerFactory.getLogger(ContainerizeStage.class);

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String dockerfile = (String) config.getOrDefault("dockerfile", "Dockerfile");
        
        String image = "myapp";
        String tag = context.getVariable("GIT_COMMIT");
        if (tag == null) tag = "latest";
        
        log.info("Building Docker image: {}:{}", image, tag);
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
            
            log.info("Containerized successfully: {}:{}", image, tag);
            return "Containerized successfully: " + image + ":" + tag;
        } catch (Exception e) {
            log.error("Containerization failed: {}", e.getMessage());
            throw new RuntimeException("Containerization failed: " + e.getMessage());
        }
    }
}
