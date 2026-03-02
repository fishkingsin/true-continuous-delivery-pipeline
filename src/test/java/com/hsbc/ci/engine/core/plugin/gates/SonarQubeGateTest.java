package com.hsbc.ci.engine.core.plugin.gates;

import com.hsbc.ci.engine.core.plugin.GateResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SonarQubeGateTest {

    private final SonarQubeGate gate = new SonarQubeGate();

    @Test
    void getType_returnsSonarqube() {
        assertEquals("sonarqube", gate.getType());
    }

    @Test
    void evaluate_passes_whenAllMetricsWithinThresholds() {
        Map<String, Object> config = Map.of(
            "minCoverage", 80,
            "maxBlocker", 0,
            "maxCritical", 0,
            "maxMajor", 5
        );
        Map<String, Object> context = createContextWithSonarStage(85, 0, 0, 3);

        GateResult result = gate.evaluate(config, context);

        assertTrue(result.isPassed());
    }

    @Test
    void evaluate_fails_whenCoverageBelowThreshold() {
        Map<String, Object> config = Map.of("minCoverage", 80);
        Map<String, Object> context = createContextWithSonarStage(70, 0, 0, 0);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("coverage"));
    }

    @Test
    void evaluate_fails_whenBlockerIssuesExceedThreshold() {
        Map<String, Object> config = Map.of("maxBlocker", 0);
        Map<String, Object> context = createContextWithSonarStage(90, 2, 0, 0);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("blocker"));
    }

    @Test
    void evaluate_fails_whenCriticalIssuesExceedThreshold() {
        Map<String, Object> config = Map.of("maxCritical", 1);
        Map<String, Object> context = createContextWithSonarStage(90, 0, 3, 0);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("critical"));
    }

    @Test
    void evaluate_fails_whenMajorIssuesExceedThreshold() {
        Map<String, Object> config = Map.of("maxMajor", 2);
        Map<String, Object> context = createContextWithSonarStage(90, 0, 0, 5);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("major"));
    }

    @Test
    void evaluate_passes_withQualityGateRating() {
        Map<String, Object> config = Map.of("minQualityGateRating", 1);
        Map<String, Object> context = createContextWithSonarStage(90, 0, 0, 0, 1);

        GateResult result = gate.evaluate(config, context);

        assertTrue(result.isPassed());
    }

    @Test
    void evaluate_fails_whenQualityGateRatingBelowThreshold() {
        Map<String,Object> config = Map.of("minQualityGateRating", 1);
        Map<String, Object> context = createContextWithSonarStage(90, 0, 0, 0, 0);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("quality gate"));
    }

    @Test
    void evaluate_warns_whenNoSonarStageFound() {
        Map<String, Object> config = Map.of("minCoverage", 80);
        Map<String, Object> context = Map.of(
            "pipelineName", "test",
            "stageResults", Map.of()
        );

        GateResult result = gate.evaluate(config, context);

        assertEquals("WARN", result.getSeverity());
        assertTrue(result.getMessage().contains("No SonarQube"));
    }

    @Test
    void evaluate_usesDefaults_whenNoConfig() {
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> context = createContextWithSonarStage(50, 0, 0, 0);

        GateResult result = gate.evaluate(config, context);

        assertTrue(result.isPassed());
    }

    @Test
    void evaluate_parsesCoverageFromOutput() {
        Map<String, Object> config = Map.of("minCoverage", 80);
        Map<String, Object> context = new HashMap<>();
        
        Map<String, Object> stageResults = new HashMap<>();
        Map<String, Object> stageData = new HashMap<>();
        stageData.put("success", true);
        stageData.put("output", "Line coverage: 75%");
        stageResults.put("sonar-analysis", stageData);
        context.put("stageResults", stageResults);

        GateResult result = gate.evaluate(config, context);

        assertFalse(result.isPassed());
    }

    @Test
    void evaluate_combinesMultipleThresholds() {
        Map<String, Object> config = Map.of(
            "minCoverage", 80,
            "maxBlocker", 0,
            "maxCritical", 0,
            "maxMajor", 5
        );
        Map<String, Object> context = createContextWithSonarStage(85, 0, 0, 3);

        GateResult result = gate.evaluate(config, context);

        assertTrue(result.isPassed());
        assertEquals("INFO", result.getSeverity());
    }

    private Map<String, Object> createContextWithSonarStage(int coverage, int blocker, int critical, int major) {
        return createContextWithSonarStage(coverage, blocker, critical, major, -1);
    }

    private Map<String, Object> createContextWithSonarStage(int coverage, int blocker, int critical, int major, int qualityGateRating) {
        Map<String, Object> context = new HashMap<>();
        
        Map<String, Object> stageResults = new HashMap<>();
        Map<String, Object> stageData = new HashMap<>();
        stageData.put("success", true);
        stageData.put("durationMs", 120000);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("coverage", coverage);
        metadata.put("blocker", blocker);
        metadata.put("critical", critical);
        metadata.put("major", major);
        if (qualityGateRating >= 0) {
            metadata.put("qualityGateRating", qualityGateRating);
        }
        stageData.put("metadata", metadata);
        
        stageResults.put("sonar-analysis", stageData);
        context.put("stageResults", stageResults);
        
        return context;
    }
}
