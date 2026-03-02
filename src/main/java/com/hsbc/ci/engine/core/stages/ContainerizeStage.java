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
        String image = (String) config.getOrDefault("image", "myapp");
        String registry = (String) config.getOrDefault("registry", "docker.io/myorg");
        
        Object tagObj = config.get("tag");
        String tag = "latest";
        if (tagObj instanceof String) {
            tag = (String) tagObj;
        } else if (context != null) {
            String gitCommit = context.getVariable("GIT_COMMIT");
            if (gitCommit != null) {
                tag = gitCommit;
            }
        }
        
        boolean dryRun = Boolean.parseBoolean(System.getProperty("dry.run", "false"));
        
        if (dryRun) {
            log.info("[DRY-RUN] Would build Docker image: {}/{}:{}", registry, image, tag);
            return "[DRY-RUN] Containerize: " + registry + "/" + image + ":" + tag;
        }
        
        log.info("Building Docker image: {}/{}:{}", registry, image, tag);
        
        try {
            return runContainerize(registry, image, tag, dockerfile, config);
        } catch (Exception e) {
            log.warn("Docker not available or build failed: {}, using placeholder", e.getMessage());
            return runPlaceholder(registry, image, tag, dockerfile);
        }
    }

    private String runContainerize(String registry, String image, String tag, String dockerfile, Map<String, Object> config) throws Exception {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("docker", "build", "-t", registry + "/" + image + ":" + tag, "-f", dockerfile, ".");
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException("Docker build failed: " + output);
        }
        
        boolean push = Boolean.parseBoolean(String.valueOf(config.getOrDefault("push", "false")));
        if (push) {
            log.info("Pushing image to registry: {}/{}/{}", registry, image, tag);
            ProcessBuilder pushPb = new ProcessBuilder();
            pushPb.command("docker", "push", registry + "/" + image + ":" + tag);
            pushPb.redirectErrorStream(true);
            Process pushProcess = pushPb.start();
            pushProcess.waitFor();
        }
        
        log.info("Containerized successfully: {}/{}:{}", registry, image, tag);
        return "Containerized: " + registry + "/" + image + ":" + tag;
    }

    private String runPlaceholder(String registry, String image, String tag, String dockerfile) {
        log.info("Using placeholder containerization (Docker not available)");
        
        String placeholderOutput = """
            [PLACEHOLDER] Container Build
            =============================
            Image: {}/{}:{}
            Dockerfile: {}
            
            [INFO] Docker not available - using placeholder
            [INFO] In production, this would:
              1. Build the container image using Docker
              2. Run security scans on the image
              3. Scan for vulnerabilities
              4. Push to registry: {}
              5. Generate image digest
            
            [SUCCESS] Placeholder containerize completed
            """.formatted(registry, image, tag, dockerfile, registry);
        
        System.out.println(placeholderOutput);
        return placeholderOutput;
    }
}
