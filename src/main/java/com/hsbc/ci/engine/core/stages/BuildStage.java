package com.hsbc.ci.engine.core.stages;

import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;
import java.util.Map;

@Component
public class BuildStage implements Stage {

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
                throw new RuntimeException("Build failed with exit code: " + exitCode);
            }
            return "Build completed successfully";
        } catch (Exception e) {
            throw new RuntimeException("Build failed: " + e.getMessage());
        }
    }
}
