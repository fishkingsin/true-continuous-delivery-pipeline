package com.hsbc.ci.engine.core.stages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;
import java.util.Map;

@Component
public class TestStage implements Stage {

    private static final Logger log = LoggerFactory.getLogger(TestStage.class);

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
                log.error("Tests failed with exit code: {}", exitCode);
                throw new RuntimeException("Tests failed with exit code: " + exitCode);
            }
            log.info("Tests passed successfully");
            return "Tests passed successfully";
        } catch (Exception e) {
            log.error("Tests failed: {}", e.getMessage());
            throw new RuntimeException("Tests failed: " + e.getMessage());
        }
    }
}
