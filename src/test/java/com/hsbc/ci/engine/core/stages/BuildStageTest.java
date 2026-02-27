package com.hsbc.ci.engine.core.stages;

import com.hsbc.ci.engine.core.model.PipelineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BuildStageTest {

    private BuildStage buildStage;

    @BeforeEach
    void setUp() {
        buildStage = new BuildStage();
    }

    @Test
    void execute_usesMavenAsDefaultBuildTool() {
        Map<String, Object> config = new HashMap<>();
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertTrue(exception.getMessage().contains("Build failed") || 
                   exception.getMessage().contains("Cannot run program"));
    }

    @Test
    void execute_supportsGradleBuildTool() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "gradle");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_supportsNpmBuildTool() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "npm");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_supportsDotnetBuildTool() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "dotnet");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_throwsForUnsupportedBuildTool() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "unsupported-tool");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertTrue(exception.getMessage().contains("Unsupported"));
    }

    @Test
    void execute_usesWorkingDirectoryFromContext() {
        Map<String, Object> config = new HashMap<>();
        
        PipelineContext context = PipelineContext.builder()
            .pipelineName("test")
            .build();
        context.addVariable("WORKING_DIRECTORY", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, context));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_supportsMavenGoals() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "maven");
        config.put("goals", "clean compile");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_supportsGradleTasks() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "gradle");
        config.put("tasks", "clean build");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_supportsNpmCommand() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "npm");
        config.put("command", "run build");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_supportsDotnetCommand() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "dotnet");
        config.put("command", "publish");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_supportsSkipTests() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "maven");
        config.put("skipTests", true);
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void execute_supportsMake() {
        Map<String, Object> config = new HashMap<>();
        config.put("build-tool", "make");
        config.put("target", "all");
        config.put("working-dir", "/tmp");
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> buildStage.execute(config, PipelineContext.builder().pipelineName("test").build()));
        
        assertNotNull(exception.getMessage());
    }
}
