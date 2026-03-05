package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.model.StageResult;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PipelineOutputFormatterTest {

    @Test
    void shouldFormatSuccessfulPipeline() {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        StageResult stageResult = StageResult.builder()
            .stageName("build")
            .success(true)
            .build();
        stages.put("build", stageResult);

        String output = PipelineOutputFormatter.formatPipelineOutput("test-pipeline", "prod", stages, false);

        assertNotNull(output);
        assertTrue(output.contains("CD Pipeline: test-pipeline"));
        assertTrue(output.contains("[SUCCESS] Pipeline completed successfully"));
        assertTrue(output.contains("✓"));
        assertTrue(output.contains("100%"));
    }

    @Test
    void shouldFormatFailedPipeline() {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        StageResult stageResult = StageResult.builder()
            .stageName("test")
            .success(false)
            .build();
        stages.put("test", stageResult);

        String output = PipelineOutputFormatter.formatPipelineOutput("test-pipeline", "dev", stages, true);

        assertNotNull(output);
        assertTrue(output.contains("[FAILED] Pipeline execution failed"));
        assertTrue(output.contains("✗"));
        assertTrue(output.contains("0%"));
    }

    @Test
    void shouldHandleNullEnvironment() {
        Map<String, StageResult> stages = new LinkedHashMap<>();

        String output = PipelineOutputFormatter.formatPipelineOutput("test-pipeline", null, stages, false);

        assertTrue(output.contains("[INFO] Environment: default"));
    }

    @Test
    void shouldHandleEmptyStages() {
        Map<String, StageResult> stages = new LinkedHashMap<>();

        String output = PipelineOutputFormatter.formatPipelineOutput("test-pipeline", "prod", stages, false);

        assertTrue(output.contains("(No stages executed)"));
        assertTrue(output.contains("[SUCCESS] Pipeline completed successfully"));
    }

    @Test
    void shouldFormatMultipleStages() {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        
        StageResult buildResult = StageResult.builder()
            .stageName("build")
            .success(true)
            .build();
        stages.put("build", buildResult);
        
        StageResult testResult = StageResult.builder()
            .stageName("test")
            .success(true)
            .build();
        stages.put("test", testResult);
        
        StageResult scanResult = StageResult.builder()
            .stageName("security-scan")
            .success(true)
            .build();
        stages.put("security-scan", scanResult);

        String output = PipelineOutputFormatter.formatPipelineOutput("multi-stage", "staging", stages, false);

        assertTrue(output.contains("build"));
        assertTrue(output.contains("test"));
        assertTrue(output.contains("security-scan"));
        assertTrue(output.lines().filter(l -> l.contains("✓")).count() >= 3);
    }

    @Test
    void shouldContainProperBorders() {
        Map<String, StageResult> stages = new LinkedHashMap<>();

        String output = PipelineOutputFormatter.formatPipelineOutput("test-pipeline", "prod", stages, false);

        assertTrue(output.contains("╔"));
        assertTrue(output.contains("╗"));
        assertTrue(output.contains("╠"));
        assertTrue(output.contains("╣"));
        assertTrue(output.contains("╚"));
    }

    @Test
    void shouldFormatPipelineNameInHeader() {
        String pipelineName = "my-java-app-pipeline";
        Map<String, StageResult> stages = new LinkedHashMap<>();

        String output = PipelineOutputFormatter.formatPipelineOutput(pipelineName, "prod", stages, false);

        assertTrue(output.contains("CD Pipeline: " + pipelineName));
    }

    @Test
    void shouldIncludePipelineInfoHeader() {
        String pipelineName = "build-pipeline";
        String environment = "staging";
        Map<String, StageResult> stages = new LinkedHashMap<>();

        String output = PipelineOutputFormatter.formatPipelineOutput(pipelineName, environment, stages, false);

        assertTrue(output.contains("[INFO] Loading pipeline: " + pipelineName));
        assertTrue(output.contains("[INFO] Environment: " + environment));
    }

    @Test
    void shouldTruncateLongStageName() {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        String longStageName = "this-is-a-very-long-stage-name-that-exceeds-the-maximum-allowed-length";
        
        StageResult result = StageResult.builder()
            .stageName(longStageName)
            .success(true)
            .build();
        stages.put(longStageName, result);

        String output = PipelineOutputFormatter.formatPipelineOutput("test-pipeline", "prod", stages, false);

        // Should contain truncated name with ellipsis
        assertTrue(output.contains("..."));
        // Should not contain the full long name
        assertFalse(output.contains(longStageName));
        // Should maintain box structure
        assertTrue(output.contains("✓"));
        assertTrue(output.contains("[████████████]"));
    }

    @Test
    void shouldHandleNullStagesMap() {
        String output = PipelineOutputFormatter.formatPipelineOutput("test-pipeline", "prod", null, false);

        assertNotNull(output);
        assertTrue(output.contains("(No stages executed)"));
        assertTrue(output.contains("[SUCCESS] Pipeline completed successfully"));
    }

    @Test
    void shouldDisplayCorrectProgressBars() {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        
        StageResult success = StageResult.builder()
            .stageName("success-stage")
            .success(true)
            .build();
        stages.put("success-stage", success);
        
        StageResult failure = StageResult.builder()
            .stageName("failure-stage")
            .success(false)
            .build();
        stages.put("failure-stage", failure);

        String output = PipelineOutputFormatter.formatPipelineOutput("test-pipeline", "prod", stages, true);

        assertTrue(output.contains("████████████")); // success bar
        assertTrue(output.contains("............")); // failure bar
        assertTrue(output.lines().filter(l -> l.contains("100%")).count() >= 1);
        assertTrue(output.lines().filter(l -> l.contains("0%")).count() >= 1);
    }

    @Test
    void shouldMaintainBoxStructure() {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        stages.put("stage1", StageResult.builder().stageName("stage1").success(true).build());

        String output = PipelineOutputFormatter.formatPipelineOutput("pipeline", "env", stages, false);
        
        // Each line should fit within default width (78 chars)
        long lineCount = output.lines().count();
        assertTrue(lineCount >= 7, "Should have at least 7 lines (borders, title, stages, status)");
        
        // Box lines should have left and right borders
        output.lines()
            .filter(l -> l.contains("║"))
            .forEach(l -> {
                assertTrue(l.startsWith("║"), "Box line should start with ║: " + l);
                assertTrue(l.endsWith("║"), "Box line should end with ║: " + l);
            });
    }

    @Test
    void shouldFormatInfoHeaderSeparately() {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        
        String output = PipelineOutputFormatter.formatPipelineOutput("app-pipeline", "production", stages, false);
        
        // Header should appear before the box
        int infoIndex = output.indexOf("[INFO]");
        int boxIndex = output.indexOf("╔");
        assertTrue(infoIndex < boxIndex, "INFO header should appear before the box");
    }

    @Test
    void shouldSwitchSuccessFailureStatus() {
        Map<String, StageResult> successStages = new LinkedHashMap<>();
        successStages.put("build", StageResult.builder().stageName("build").success(true).build());

        String successOutput = PipelineOutputFormatter.formatPipelineOutput("pipeline", "env", successStages, false);
        String failureOutput = PipelineOutputFormatter.formatPipelineOutput("pipeline", "env", successStages, true);

        assertTrue(successOutput.contains("[SUCCESS]"));
        assertFalse(successOutput.contains("[FAILED]"));
        
        assertTrue(failureOutput.contains("[FAILED]"));
        assertFalse(failureOutput.contains("[SUCCESS]"));
    }

    @Test
    void shouldHandleSingleStage() {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        stages.put("single", StageResult.builder().stageName("single").success(true).build());

        String output = PipelineOutputFormatter.formatPipelineOutput("test", "env", stages, false);

        assertTrue(output.contains("single"));
        assertTrue(output.contains("✓"));
        assertTrue(output.contains("[SUCCESS]"));
    }

    @Test
    void shouldPreservePipelineNameInMultipleFormats() {
        String[] pipelineNames = {"build-pipeline", "deploy-prod", "test_suite", "microservice-cd"};
        
        for (String name : pipelineNames) {
            Map<String, StageResult> stages = new LinkedHashMap<>();
            String output = PipelineOutputFormatter.formatPipelineOutput(name, "env", stages, false);
            assertTrue(output.contains("CD Pipeline: " + name), "Pipeline name should be in output for " + name);
        }
    }
}
