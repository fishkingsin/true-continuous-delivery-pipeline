package com.hsbc.ci.engine.core.stages;

import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;
import java.util.Map;

@Component
public class TestStage implements Stage {

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String testType = (String) config.getOrDefault("test-type", "unit");
        
        System.out.println("  Running tests: " + testType);
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("mvn", "test");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Tests failed with exit code: " + exitCode);
            }
            return "Tests passed successfully";
        } catch (Exception e) {
            throw new RuntimeException("Tests failed: " + e.getMessage());
        }
    }
}
