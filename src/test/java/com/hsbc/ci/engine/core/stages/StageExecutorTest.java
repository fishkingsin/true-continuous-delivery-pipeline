package com.hsbc.ci.engine.core.stages;

import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.StageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class StageExecutorTest {

    private StageExecutor stageExecutor;
    private PipelineContext context;

    @BeforeEach
    void setUp() {
        stageExecutor = new StageExecutor();
        context = PipelineContext.builder()
            .pipelineName("test-pipeline")
            .build();
    }

    @Test
    void execute_returnsFailure_whenStageTypeIsUnknown() {
        StageResult result = stageExecutor.execute("unknown-type", new HashMap<>(), context);

        assertFalse(result.isSuccess());
        assertEquals("unknown-type", result.getStageName());
        assertTrue(result.getOutput().contains("Unknown stage type"));
    }

    @Test
    void execute_hasBuildStage() {
        assertDoesNotThrow(() -> {
            StageResult result = stageExecutor.execute("build", new HashMap<>(), context);
            assertNotNull(result);
        });
    }

    @Test
    void execute_hasTestStage() {
        assertDoesNotThrow(() -> {
            StageResult result = stageExecutor.execute("test", new HashMap<>(), context);
            assertNotNull(result);
        });
    }

    @Test
    void execute_hasDeployStage() {
        assertDoesNotThrow(() -> {
            StageResult result = stageExecutor.execute("deploy", new HashMap<>(), context);
            assertNotNull(result);
        });
    }

    @Test
    void execute_hasContainerizeStage() {
        assertDoesNotThrow(() -> {
            StageResult result = stageExecutor.execute("containerize", new HashMap<>(), context);
            assertNotNull(result);
        });
    }

    @Test
    void execute_calculatesDuration() {
        long startTime = System.currentTimeMillis();
        StageResult result = stageExecutor.execute("build", new HashMap<>(), context);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(result.getDurationMs() >= 0);
        assertTrue(result.getDurationMs() <= duration + 100);
    }

    @Test
    void execute_returnsResultWithStageName() {
        StageResult result = stageExecutor.execute("build", new HashMap<>(), context);

        assertNotNull(result.getStageName());
        assertEquals("build", result.getStageName());
    }

    @Test
    void execute_returnsResultWithOutput() {
        StageResult result = stageExecutor.execute("build", new HashMap<>(), context);

        assertNotNull(result.getOutput());
    }

    @Test
    void execute_handlesNullConfig() {
        assertDoesNotThrow(() -> {
            StageResult result = stageExecutor.execute("build", null, context);
            assertNotNull(result);
        });
    }

    @Test
    void execute_handlesNullContext() {
        assertDoesNotThrow(() -> {
            StageResult result = stageExecutor.execute("build", new HashMap<>(), null);
            assertNotNull(result);
        });
    }
}
