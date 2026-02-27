package com.hsbc.ci.engine.core.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PipelineContextTest {

    @Test
    void builder_withNameAndEnvironment_setsCorrectValues() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .environment("dev")
            .build();
        
        assertEquals("test-pipeline", context.getPipelineName());
        assertEquals("dev", context.getEnvironment());
    }

    @Test
    void builder_defaultDryRun_isFalse() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        
        assertFalse(context.isDryRun());
    }

    @Test
    void addVariable_storesVariable() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        
        context.addVariable("GIT_COMMIT", "abc123");
        
        assertEquals("abc123", context.getVariable("GIT_COMMIT"));
    }

    @Test
    void getVariable_withNonExistent_returnsNull() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        
        assertNull(context.getVariable("NONEXISTENT"));
    }

    @Test
    void addStageResult_storesResult() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        
        StageResult stageResult = StageResult.builder()
            .stageName("build")
            .success(true)
            .output("Build completed")
            .build();
        
        context.addStageResult("build", stageResult);
        
        assertNotNull(context.getStageResults().get("build"));
        assertTrue(context.getStageResults().get("build").isSuccess());
    }
}
