package com.hsbc.ci.engine.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PipelineResultTest {

    @Test
    void success_withContext_returnsSuccessTrue() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        
        PipelineResult result = PipelineResult.success(context);
        
        assertTrue(result.isSuccess());
        assertNull(result.getError());
        assertEquals(context, result.getContext());
    }

    @Test
    void failed_withError_returnsSuccessFalse() {
        PipelineResult result = PipelineResult.failed("Pipeline not found");
        
        assertFalse(result.isSuccess());
        assertEquals("Pipeline not found", result.getError());
        assertNull(result.getContext());
    }
}
