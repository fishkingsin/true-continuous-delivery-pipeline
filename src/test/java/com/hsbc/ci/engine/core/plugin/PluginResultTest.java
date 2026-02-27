package com.hsbc.ci.engine.core.plugin;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PluginResultTest {

    @Test
    void success_withMessage_returnsSuccessTrue() {
        PluginResult result = PluginResult.success("Build completed");
        
        assertTrue(result.isSuccess());
        assertEquals("Build completed", result.getMessage());
    }

    @Test
    void success_withMessageAndData_returnsSuccessWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("artifact", "myapp.jar");
        
        PluginResult result = PluginResult.success("Build completed", data);
        
        assertTrue(result.isSuccess());
        assertEquals("Build completed", result.getMessage());
        assertEquals("myapp.jar", result.getData().get("artifact"));
    }

    @Test
    void failure_returnsSuccessFalse() {
        PluginResult result = PluginResult.failure("Build failed");
        
        assertFalse(result.isSuccess());
        assertEquals("Build failed", result.getMessage());
    }

    @Test
    void builder_allowsCustomDuration() {
        PluginResult result = PluginResult.builder()
            .success(true)
            .message("Test")
            .durationMs(1500)
            .build();
        
        assertEquals(1500, result.getDurationMs());
    }
}
