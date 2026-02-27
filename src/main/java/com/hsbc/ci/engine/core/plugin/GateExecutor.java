package com.hsbc.ci.engine.core.plugin;

import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.StageResult;
import com.hsbc.ci.engine.core.plugin.gates.CoverageGate;
import com.hsbc.ci.engine.core.plugin.gates.SecurityScanGate;
import com.hsbc.ci.engine.core.plugin.gates.SonarQubeGate;
import com.hsbc.ci.engine.core.plugin.gates.TestPassedGate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GateExecutor {

    private static final Logger log = LoggerFactory.getLogger(GateExecutor.class);

    private final PluginManager pluginManager;
    private final Map<String, GatePlugin> registeredGates = new HashMap<>();

    public GateExecutor(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        registerBuiltInGates();
    }

    private void registerBuiltInGates() {
        registerGate(new TestPassedGate());
        registerGate(new CoverageGate());
        registerGate(new SecurityScanGate());
        registerGate(new SonarQubeGate());
        
        pluginManager.registerGate(new TestPassedGate());
        pluginManager.registerGate(new CoverageGate());
        pluginManager.registerGate(new SecurityScanGate());
        pluginManager.registerGate(new SonarQubeGate());
    }

    public void registerGate(GatePlugin gate) {
        registeredGates.put(gate.getType(), gate);
        log.info("Registered gate: {}", gate.getType());
    }

    public GateResult executeGate(String gateType, Map<String, Object> config, PipelineContext context) {
        GatePlugin gate = registeredGates.get(gateType);
        
        if (gate == null) {
            var pluginGate = pluginManager.getGatePlugin(gateType);
            if (pluginGate != null) {
                Map<String, Object> pluginContext = buildContext(context);
                return pluginGate.evaluate(config, pluginContext);
            }
            log.warn("Gate not found: {}", gateType);
            return GateResult.fail("Gate not found: " + gateType);
        }

        Map<String, Object> gateContext = buildContext(context);
        return gate.evaluate(config, gateContext);
    }

    public List<GateResult> executeGates(List<String> gateTypes, Map<String, Object> config, PipelineContext context) {
        List<GateResult> results = new ArrayList<>();
        
        for (String gateType : gateTypes) {
            GateResult result = executeGate(gateType, config, context);
            results.add(result);
            
            if (!result.isPassed()) {
                log.error("Gate failed: {} - {}", gateType, result.getMessage());
                break;
            }
        }
        
        return results;
    }

    public boolean evaluateAllGates(List<String> gateTypes, Map<String, Object> config, PipelineContext context) {
        List<GateResult> results = executeGates(gateTypes, config, context);
        
        for (GateResult result : results) {
            if (!result.isPassed()) {
                return false;
            }
        }
        
        return true;
    }

    private Map<String, Object> buildContext(PipelineContext context) {
        Map<String, Object> ctx = new HashMap<>();
        
        if (context != null) {
            ctx.put("pipelineName", context.getPipelineName());
            ctx.put("environment", context.getEnvironment());
            ctx.put("variables", context.getVariables());
            
            Map<String, StageResult> stageResults = context.getStageResults();
            Map<String, Object> resultsMap = new HashMap<>();
            
            for (Map.Entry<String, StageResult> entry : stageResults.entrySet()) {
                StageResult sr = entry.getValue();
                Map<String, Object> stageData = new HashMap<>();
                stageData.put("success", sr.isSuccess());
                stageData.put("durationMs", sr.getDurationMs());
                stageData.put("output", sr.getOutput());
                if (sr.getMetadata() != null) {
                    stageData.put("metadata", sr.getMetadata());
                }
                resultsMap.put(entry.getKey(), stageData);
            }
            ctx.put("stageResults", resultsMap);
        }
        
        return ctx;
    }

    public Set<String> getRegisteredGateTypes() {
        return new HashSet<>(registeredGates.keySet());
    }
}
