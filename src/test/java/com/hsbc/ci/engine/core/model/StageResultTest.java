package com.hsbc.ci.engine.core.model;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StageResultTest {

    @Test
    void builder_withAllFields_setsCorrectValues() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("artifact", "app.jar");
        
        StageResult result = StageResult.builder()
            .stageName("build")
            .success(true)
            .output("Build completed")
            .durationMs(5000)
            .metadata(metadata)
            .build();
        
        assertEquals("build", result.getStageName());
        assertTrue(result.isSuccess());
        assertEquals("Build completed", result.getOutput());
        assertEquals(5000, result.getDurationMs());
        assertEquals("app.jar", result.getMetadata().get("artifact"));
    }

    @Test
    void builder_defaults_successToFalse() {
        StageResult result = StageResult.builder()
            .stageName("test")
            .build();
        
        assertFalse(result.isSuccess());
    }

    @Test
    void builder_defaults_outputToNull() {
        StageResult result = StageResult.builder()
            .stageName("test")
            .build();
        
        assertNull(result.getOutput());
    }
}
