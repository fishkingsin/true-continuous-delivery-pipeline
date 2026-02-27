package com.hsbc.ci.engine.core.plugin.gates;

import com.hsbc.ci.engine.core.plugin.GatePlugin;
import com.hsbc.ci.engine.core.plugin.GateResult;

import java.util.Map;

public class SecurityScanGate implements GatePlugin {

    private static final int DEFAULT_MAX_CRITICAL = 0;
    private static final int DEFAULT_MAX_HIGH = 0;

    @Override
    public String getType() {
        return "security-scan";
    }

    @Override
    public GateResult evaluate(Map<String, Object> config, Map<String, Object> context) {
        int maxCritical = DEFAULT_MAX_CRITICAL;
        int maxHigh = DEFAULT_MAX_HIGH;
        
        Object criticalObj = config.get("maxCritical");
        if (criticalObj instanceof Number) {
            maxCritical = ((Number) criticalObj).intValue();
        }
        
        Object highObj = config.get("maxHigh");
        if (highObj instanceof Number) {
            maxHigh = ((Number) highObj).intValue();
        }
        
        Object stageResultsObj = context.get("stageResults");
        
        if (!(stageResultsObj instanceof Map)) {
            return GateResult.fail("No stage results available in context");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stageResults = (Map<String, Object>) stageResultsObj;
        
        for (Map.Entry<String, Object> entry : stageResults.entrySet()) {
            String stageName = entry.getKey();
            
            if (stageName.toLowerCase().contains("security") || 
                stageName.toLowerCase().contains("scan")) {
                
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stageData = (Map<String, Object>) entry.getValue();
                    
                    Object metadata = stageData.get("metadata");
                    if (metadata instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> meta = (Map<String, Object>) metadata;
                        
                        int critical = getIntValue(meta, "critical", 0);
                        int high = getIntValue(meta, "high", 0);
                        int medium = getIntValue(meta, "medium", 0);
                        int low = getIntValue(meta, "low", 0);
                        
                        if (critical > maxCritical) {
                            return GateResult.fail(
                                "Security gate failed: " + critical + " critical vulnerabilities (max: " + maxCritical + ")"
                            );
                        }
                        
                        if (high > maxHigh) {
                            return GateResult.fail(
                                "Security gate failed: " + high + " high vulnerabilities (max: " + maxHigh + ")"
                            );
                        }
                        
                        return GateResult.pass(
                            "Security gate passed: " + critical + " critical, " + high + " high, " + medium + " medium, " + low + " low"
                        );
                    }
                    
                    Object output = stageData.get("output");
                    if (output instanceof String) {
                        return evaluateFromOutput((String) output, maxCritical, maxHigh);
                    }
                }
            }
        }
        
        return GateResult.warn("No security scan results found");
    }

    private GateResult evaluateFromOutput(String output, int maxCritical, int maxHigh) {
        int critical = countVulnerabilities(output, "CRITICAL");
        int high = countVulnerabilities(output, "HIGH");
        
        if (critical > maxCritical) {
            return GateResult.fail(
                "Security gate failed: " + critical + " critical vulnerabilities found"
            );
        }
        
        if (high > maxHigh) {
            return GateResult.fail(
                "Security gate failed: " + high + " high vulnerabilities found"
            );
        }
        
        return GateResult.pass("Security scan passed - no threshold violations");
    }

    private int countVulnerabilities(String output, String severity) {
        String searchPattern = severity.toLowerCase() + " vulnerabilities";
        int idx = output.toLowerCase().indexOf(searchPattern);
        
        if (idx >= 0) {
            String remaining = output.substring(idx);
            StringBuilder sb = new StringBuilder();
            for (char c : remaining.toCharArray()) {
                if (Character.isDigit(c)) {
                    sb.append(c);
                } else if (sb.length() > 0) {
                    break;
                }
            }
            try {
                return Integer.parseInt(sb.toString());
            } catch (NumberFormatException e) {
            }
        }
        
        return 0;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
}
