package com.hsbc.ci.engine.core.plugin.gates;

import com.hsbc.ci.engine.core.plugin.GatePlugin;
import com.hsbc.ci.engine.core.plugin.GateResult;

import java.util.Map;

public class TestPassedGate implements GatePlugin {

    @Override
    public String getType() {
        return "test-passed";
    }

    @Override
    public GateResult evaluate(Map<String, Object> config, Map<String, Object> context) {
        Object stageResultsObj = context.get("stageResults");
        
        if (!(stageResultsObj instanceof Map)) {
            return GateResult.fail("No stage results available in context");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stageResults = (Map<String, Object>) stageResultsObj;
        
        boolean testStageFound = false;
        boolean testPassed = true;
        
        for (Map.Entry<String, Object> entry : stageResults.entrySet()) {
            String stageName = entry.getKey();
            
            if (stageName.toLowerCase().contains("test")) {
                testStageFound = true;
                
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stageData = (Map<String, Object>) entry.getValue();
                    Object success = stageData.get("success");
                    
                    if (success instanceof Boolean) {
                        testPassed = (Boolean) success;
                    }
                    
                    if (!testPassed) {
                        Object output = stageData.get("output");
                        String message = output != null ? 
                            "Test stage '" + stageName + "' failed: " + output : 
                            "Test stage '" + stageName + "' failed";
                        return GateResult.fail(message);
                    }
                }
            }
        }
        
        if (!testStageFound) {
            return GateResult.warn("No test stage found in pipeline - cannot verify test passed gate");
        }
        
        return GateResult.pass("All test stages passed successfully");
    }
}
