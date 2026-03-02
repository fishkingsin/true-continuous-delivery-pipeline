package com.hsbc.ci.engine.core.plugin.gates;

import com.hsbc.ci.engine.core.plugin.GateResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityScanGateTest {

    private final SecurityScanGate gate = new SecurityScanGate();

    @Test
    void getType_returnsSecurityScan() {
        assertEquals("security-scan", gate.getType());
    }

    @Test
    void evaluate_passes_whenNoCriticalVulnerabilities() {
        Map<String, Object> config = Map.of("maxCritical", 0, "maxHigh", 5);
        Map<String, Object> context = createContextWithSecurityStage(0, 2, 3, 4);

        GateResult result = gate.evaluate(config, context);

        assertTrue(result.isPassed());
        assertNotNull(result.getMessage());
    }

    @Test
    void evaluate_fails_whenCriticalExceedsThreshold() {
        Map<String, Object> config = Map.of("maxCritical", 0, "maxHigh", 5);
        Map<String, Object> context = createContextWithSecurityStage(2, 0, 0, 0);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("critical vulnerabilities"));
    }

    @Test
    void evaluate_fails_whenHighExceedsThreshold() {
        Map<String, Object> config = Map.of("maxCritical", 0, "maxHigh", 2);
        Map<String, Object> context = createContextWithSecurityStage(0, 5, 0, 0);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("high vulnerabilities"));
    }

    @Test
    void evaluate_passes_withinThresholds() {
        Map<String, Object> config = Map.of("maxCritical", 1, "maxHigh", 3);
        Map<String, Object> context = createContextWithSecurityStage(1, 3, 10, 20);

        GateResult result = gate.evaluate(config, context);

        assertTrue(result.isPassed());
    }

    @Test
    void evaluate_warns_whenNoSecurityStageFound() {
        Map<String, Object> config = Map.of("maxCritical", 0, "maxHigh", 0);
        Map<String, Object> context = Map.of(
            "pipelineName", "test",
            "stageResults", Map.of()
        );

        GateResult result = gate.evaluate(config, context);

        assertEquals("WARN", result.getSeverity());
        assertTrue(result.getMessage().contains("No security scan results"));
    }

    @Test
    void evaluate_usesDefaults_whenNoConfig() {
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> context = createContextWithSecurityStage(0, 0, 0, 0);

        GateResult result = gate.evaluate(config, context);

        assertTrue(result.isPassed());
    }

    @Test
    void evaluate_parsesOutput_correctly() {
        Map<String, Object> config = Map.of("maxCritical", 0, "maxHigh", 0);
        Map<String, Object> context = new HashMap<>();
        
        Map<String, Object> stageResults = new HashMap<>();
        Map<String, Object> stageData = new HashMap<>();
        stageData.put("success", true);
        stageData.put("output", "Found 5 CRITICAL vulnerabilities and 10 HIGH vulnerabilities");
        stageResults.put("security-scan", stageData);
        context.put("stageResults", stageResults);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
    }

    private Map<String, Object> createContextWithSecurityStage(int critical, int high, int medium, int low) {
        Map<String, Object> context = new HashMap<>();
        
        Map<String, Object> stageResults = new HashMap<>();
        Map<String, Object> stageData = new HashMap<>();
        stageData.put("success", true);
        stageData.put("durationMs", 5000);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("critical", critical);
        metadata.put("high", high);
        metadata.put("medium", medium);
        metadata.put("low", low);
        stageData.put("metadata", metadata);
        
        stageResults.put("security-scan", stageData);
        context.put("stageResults", stageResults);
        
        return context;
    }
}
