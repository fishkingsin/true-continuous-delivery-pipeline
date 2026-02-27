package com.hsbc.ci.engine.core.stages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;
import java.util.Map;

@Component
public class BuildStage implements Stage {

    private static final Logger log = LoggerFactory.getLogger(BuildStage.class);

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String buildTool = (String) config.getOrDefault("build-tool", "maven");
        
        System.out.println("  Building with: " + buildTool);
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            if ("maven".equals(buildTool)) {
                pb.command("mvn", "clean", "package", "-DskipTests");
            } else if ("gradle".equals(buildTool)) {
                pb.command("./gradlew", "build", "-x", "test");
            }
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                log.error("Build failed with exit code: {}", exitCode);
                throw new RuntimeException("Build failed with exit code: " + exitCode);
            }
            log.info("Build completed successfully");
            return "Build completed successfully";
        } catch (Exception e) {
            log.error("Build failed: {}", e.getMessage());
            throw new RuntimeException("Build failed: " + e.getMessage());
        }
    }
}
