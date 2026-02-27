package com.hsbc.ci.engine.core.plugin.gates;

import com.hsbc.ci.engine.core.plugin.GatePlugin;
import com.hsbc.ci.engine.core.plugin.GateResult;

import java.util.Map;

public class SonarQubeGate implements GatePlugin {

    private static final int DEFAULT_MIN_QUGATE_RATING = 1;
    private static final int DEFAULT_MIN_COVERAGE = 0;

    @Override
    public String getType() {
        return "sonarqube";
    }

    @Override
    public GateResult evaluate(Map<String, Object> config, Map<String, Object> context) {
        int minCoverage = DEFAULT_MIN_COVERAGE;
        int maxBlocker = 0;
        int maxCritical = 0;
        int maxMajor = 0;
        int minQualityGateRating = DEFAULT_MIN_QUGATE_RATING;
        
        Object coverageObj = config.get("minCoverage");
        if (coverageObj instanceof Number) {
            minCoverage = ((Number) coverageObj).intValue();
        }
        
        Object blockerObj = config.get("maxBlocker");
        if (blockerObj instanceof Number) {
            maxBlocker = ((Number) blockerObj).intValue();
        }
        
        Object criticalObj = config.get("maxCritical");
        if (criticalObj instanceof Number) {
            maxCritical = ((Number) criticalObj).intValue();
        }
        
        Object majorObj = config.get("maxMajor");
        if (majorObj instanceof Number) {
            maxMajor = ((Number) majorObj).intValue();
        }
        
        Object ratingObj = config.get("minQualityGateRating");
        if (ratingObj instanceof Number) {
            minQualityGateRating = ((Number) ratingObj).intValue();
        }
        
        Object stageResultsObj = context.get("stageResults");
        
        if (!(stageResultsObj instanceof Map)) {
            return GateResult.fail("No stage results available in context");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stageResults = (Map<String, Object>) stageResultsObj;
        
        for (Map.Entry<String, Object> entry : stageResults.entrySet()) {
            String stageName = entry.getKey();
            
            if (stageName.toLowerCase().contains("sonar") || 
                stageName.toLowerCase().contains("quality")) {
                
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stageData = (Map<String, Object>) entry.getValue();
                    
                    Object metadata = stageData.get("metadata");
                    if (metadata instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> meta = (Map<String, Object>) metadata;
                        
                        return evaluateFromMetadata(meta, minCoverage, maxBlocker, maxCritical, maxMajor, minQualityGateRating);
                    }
                    
                    Object output = stageData.get("output");
                    if (output instanceof String) {
                        return evaluateFromOutput((String) output, minCoverage, maxBlocker, maxCritical, maxMajor);
                    }
                }
            }
        }
        
        return GateResult.warn("No SonarQube results found");
    }

    private GateResult evaluateFromMetadata(Map<String, Object> meta, int minCoverage, 
            int maxBlocker, int maxCritical, int maxMajor, int minQualityGateRating) {
        
        int coverage = getIntValue(meta, "coverage", -1);
        int blocker = getIntValue(meta, "blocker", -1);
        int critical = getIntValue(meta, "critical", -1);
        int major = getIntValue(meta, "major", -1);
        int qualityGateRating = getIntValue(meta, "qualityGateRating", -1);
        
        if (coverage >= 0 && coverage < minCoverage) {
            return GateResult.fail("SonarQube gate failed: coverage " + coverage + "% < " + minCoverage + "%");
        }
        
        if (blocker >= 0 && blocker > maxBlocker) {
            return GateResult.fail("SonarQube gate failed: " + blocker + " blocker issues (max: " + maxBlocker + ")");
        }
        
        if (critical >= 0 && critical > maxCritical) {
            return GateResult.fail("SonarQube gate failed: " + critical + " critical issues (max: " + maxCritical + ")");
        }
        
        if (major >= 0 && major > maxMajor) {
            return GateResult.fail("SonarQube gate failed: " + major + " major issues (max: " + maxMajor + ")");
        }
        
        if (qualityGateRating >= 0 && qualityGateRating < minQualityGateRating) {
            return GateResult.fail("SonarQube gate failed: quality gate rating " + qualityGateRating + " < " + minQualityGateRating);
        }
        
        return GateResult.pass("SonarQube gate passed");
    }

    private GateResult evaluateFromOutput(String output, int minCoverage, 
            int maxBlocker, int maxCritical, int maxMajor) {
        
        int coverage = parseCoverage(output);
        int blocker = parseIssues(output, "BLOCKER");
        int critical = parseIssues(output, "CRITICAL");
        int major = parseIssues(output, "MAJOR");
        
        if (coverage >= 0 && coverage < minCoverage) {
            return GateResult.fail("SonarQube gate failed: coverage " + coverage + "% < " + minCoverage + "%");
        }
        
        if (blocker > maxBlocker) {
            return GateResult.fail("SonarQube gate failed: " + blocker + " blocker issues");
        }
        
        if (critical > maxCritical) {
            return GateResult.fail("SonarQube gate failed: " + critical + " critical issues");
        }
        
        if (major > maxMajor) {
            return GateResult.fail("SonarQube gate failed: " + major + " major issues");
        }
        
        return GateResult.pass("SonarQube gate passed");
    }

    private int parseCoverage(String output) {
        String[] patterns = {"Coverage:", "Line coverage:"};
        for (String pattern : patterns) {
            int idx = output.indexOf(pattern);
            if (idx >= 0) {
                String remaining = output.substring(idx + pattern.length()).trim();
                StringBuilder sb = new StringBuilder();
                for (char c : remaining.toCharArray()) {
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    } else if (c == '%' && sb.length() > 0) {
                        break;
                    }
                }
                try {
                    return Integer.parseInt(sb.toString());
                } catch (NumberFormatException e) {
                }
            }
        }
        return -1;
    }

    private int parseIssues(String output, String severity) {
        String pattern = severity + " Issues:";
        int idx = output.indexOf(pattern);
        if (idx >= 0) {
            String remaining = output.substring(idx + pattern.length()).trim();
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
