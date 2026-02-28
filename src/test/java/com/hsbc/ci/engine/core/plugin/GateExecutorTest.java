package com.hsbc.ci.engine.core.plugin;

import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.StageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GateExecutorTest {

    private GateExecutor gateExecutor;
    private PluginManager pluginManager;

    @BeforeEach
    void setUp() {
        pluginManager = new PluginManager();
        gateExecutor = new GateExecutor(pluginManager);
    }

    @Test
    void executeGate_runsTestPassedGate() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        context.addStageResult("test", StageResult.builder()
            .stageName("test")
            .success(true)
            .durationMs(5000)
            .build());

        GateResult result = gateExecutor.executeGate("test-passed", new HashMap<>(), context);

        assertTrue(result.isPassed());
    }

    @Test
    void executeGate_failsTestPassedGate_whenTestFails() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        context.addStageResult("test", StageResult.builder()
            .stageName("test")
            .success(false)
            .output("Tests failed")
            .durationMs(5000)
            .build());

        GateResult result = gateExecutor.executeGate("test-passed", new HashMap<>(), context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("failed"));
    }

    @Test
    void executeGate_runsCoverageGate() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        
        context.addStageResult("test", StageResult.builder()
            .stageName("test")
            .success(true)
            .output("Coverage: 85%")
            .durationMs(5000)
            .build());

        Map<String, Object> config = new HashMap<>();
        config.put("minCoverage", 80);

        GateResult result = gateExecutor.executeGate("coverage-threshold", config, context);

        assertTrue(result.isPassed());
    }

    @Test
    void executeGate_failsCoverageGate_belowThreshold() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        
        context.addStageResult("test", StageResult.builder()
            .stageName("test")
            .success(true)
            .output("Coverage: 70%")
            .durationMs(5000)
            .build());

        Map<String, Object> config = new HashMap<>();
        config.put("minCoverage", 80);

        GateResult result = gateExecutor.executeGate("coverage-threshold", config, context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Coverage"));
    }

    @Test
    void executeGates_runsMultipleGates() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        context.addStageResult("test", StageResult.builder()
            .stageName("test")
            .success(true)
            .durationMs(5000)
            .build());

        List<GateResult> results = gateExecutor.executeGates(
            List.of("test-passed"),
            new HashMap<>(),
            context
        );

        assertEquals(1, results.size());
        assertTrue(results.get(0).isPassed());
    }

    @Test
    void evaluateAllGates_returnsTrue_whenAllPass() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        context.addStageResult("test", StageResult.builder()
            .stageName("test")
            .success(true)
            .durationMs(5000)
            .build());

        boolean result = gateExecutor.evaluateAllGates(
            List.of("test-passed"),
            new HashMap<>(),
            context
        );

        assertTrue(result);
    }

    @Test
    void evaluateAllGates_returnsFalse_whenAnyFails() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        context.addStageResult("test", StageResult.builder()
            .stageName("test")
            .success(false)
            .output("Failed")
            .durationMs(5000)
            .build());

        boolean result = gateExecutor.evaluateAllGates(
            List.of("test-passed"),
            new HashMap<>(),
            context
        );

        assertFalse(result);
    }

    @Test
    void getRegisteredGateTypes_includesBuiltInGates() {
        var gateTypes = gateExecutor.getRegisteredGateTypes();
        
        assertTrue(gateTypes.contains("test-passed"));
        assertTrue(gateTypes.contains("coverage-threshold"));
    }

    @Test
    void executeGate_returnsFail_forUnknownGate() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();

        GateResult result = gateExecutor.executeGate("unknown-gate", new HashMap<>(), context);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("not found"));
    }

    @Test
    void buildContext_includesPipelineInfo() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .environment("dev")
            .build();
        context.addVariable("KEY", "value");
        context.addStageResult("build", StageResult.builder()
            .stageName("build")
            .success(true)
            .durationMs(1000)
            .build());

        assertEquals("test-pipeline", context.getPipelineName());
        assertEquals("dev", context.getEnvironment());
        assertEquals("value", context.getVariable("KEY"));
        assertTrue(context.getStageResults().containsKey("build"));
    }
}
