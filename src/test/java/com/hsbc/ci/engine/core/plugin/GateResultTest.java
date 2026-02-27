package com.hsbc.ci.engine.core.plugin;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GateResultTest {

    @Test
    void pass_withMessage_returnsPassedTrue() {
        GateResult result = GateResult.pass("All checks passed");
        
        assertTrue(result.isPassed());
        assertEquals("All checks passed", result.getMessage());
        assertEquals("INFO", result.getSeverity());
    }

    @Test
    void fail_withMessage_returnsPassedFalse() {
        GateResult result = GateResult.fail("Security scan found critical issues");
        
        assertFalse(result.isPassed());
        assertEquals("Security scan found critical issues", result.getMessage());
        assertEquals("ERROR", result.getSeverity());
    }

    @Test
    void warn_allowsPassingWithWarning() {
        GateResult result = GateResult.warn("Coverage below threshold but acceptable");
        
        assertTrue(result.isPassed());
        assertEquals("Coverage below threshold but acceptable", result.getMessage());
        assertEquals("WARN", result.getSeverity());
    }

    @Test
    void builder_allowsCustomMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("coverage", 75);
        metrics.put("lines", 1000);
        
        GateResult result = GateResult.builder()
            .passed(true)
            .message("Quality gate passed")
            .metrics(metrics)
            .build();
        
        assertEquals(75, result.getMetrics().get("coverage"));
        assertEquals(1000, result.getMetrics().get("lines"));
    }
}
