package com.hsbc.ci.engine.core.plugin.gates;

import com.hsbc.ci.engine.core.plugin.GatePlugin;
import com.hsbc.ci.engine.core.plugin.GateResult;

import java.util.Map;

public class CoverageGate implements GatePlugin {

    private static final double DEFAULT_MIN_COVERAGE = 80.0;

    @Override
    public String getType() {
        return "coverage-threshold";
    }

    @Override
    public GateResult evaluate(Map<String, Object> config, Map<String, Object> context) {
        double minCoverage = DEFAULT_MIN_COVERAGE;
        
        Object configThreshold = config.get("minCoverage");
        if (configThreshold instanceof Number) {
            minCoverage = ((Number) configThreshold).doubleValue();
        }
        
        Object stageResultsObj = context.get("stageResults");
        
        if (!(stageResultsObj instanceof Map)) {
            return GateResult.fail("No stage results available in context");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stageResults = (Map<String, Object>) stageResultsObj;
        
        for (Map.Entry<String, Object> entry : stageResults.entrySet()) {
            String stageName = entry.getKey();
            
            if (stageName.toLowerCase().contains("test") || stageName.toLowerCase().contains("coverage")) {
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stageData = (Map<String, Object>) entry.getValue();
                    
                    Object metadata = stageData.get("metadata");
                    if (metadata instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> meta = (Map<String, Object>) metadata;
                        
                        Object coverageObj = meta.get("coverage");
                        if (coverageObj instanceof Number) {
                            double coverage = ((Number) coverageObj).doubleValue();
                            
                            if (coverage < minCoverage) {
                                return GateResult.fail(String.format(
                                    "Coverage gate failed: %.1f%% < %.1f%% (stage: %s)",
                                    coverage, minCoverage, stageName
                                ));
                            }
                            
                            return GateResult.pass(String.format(
                                "Coverage gate passed: %.1f%% >= %.1f%%", coverage, minCoverage
                            ));
                        }
                    }
                    
                    Object output = stageData.get("output");
                    if (output instanceof String) {
                        String outputStr = (String) output;
                        double coverage = parseCoverageFromOutput(outputStr);
                        
                        if (coverage > 0) {
                            if (coverage < minCoverage) {
                                return GateResult.fail(String.format(
                                    "Coverage gate failed: %.1f%% < %.1f%%",
                                    coverage, minCoverage
                                ));
                            }
                            
                            return GateResult.pass(String.format(
                                "Coverage gate passed: %.1f%% >= %.1f%%", coverage, minCoverage
                            ));
                        }
                    }
                }
            }
        }
        
        return GateResult.warn("No coverage data found in test results");
    }

    private double parseCoverageFromOutput(String output) {
        String[] patterns = {
            "Coverage:",
            "coverage:",
            "TOTAL",
            "Lines:",
            "Line coverage:"
        };
        
        for (String pattern : patterns) {
            int idx = output.indexOf(pattern);
            if (idx >= 0) {
                String remaining = output.substring(idx + pattern.length()).trim();
                StringBuilder sb = new StringBuilder();
                for (char c : remaining.toCharArray()) {
                    if (Character.isDigit(c) || c == '.') {
                        sb.append(c);
                    } else if (sb.length() > 0) {
                        break;
                    }
                }
                try {
                    return Double.parseDouble(sb.toString());
                } catch (NumberFormatException e) {
                }
            }
        }
        
        return -1;
    }
}
