package com.hsbc.ci.engine.core.config;

import com.hsbc.ci.engine.core.model.PipelineDefinition;
import com.hsbc.ci.engine.core.model.StageDefinition;
import com.hsbc.ci.engine.core.config.PipelineValidator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineValidatorTest {

    private PipelineValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PipelineValidator();
    }

    @Test
    void validate_returnsValid_whenPipelineIsCorrect() {
        PipelineDefinition pipeline = createValidPipeline();
        
        ValidationResult result = validator.validate(pipeline);
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_returnsError_whenPipelineNameIsMissing() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.setName(null);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("name is required")));
    }

    @Test
    void validate_returnsError_whenPipelineNameIsBlank() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.setName("   ");
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("name is required")));
    }

    @Test
    void validate_returnsError_whenPipelineNameHasInvalidCharacters() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.setName("invalid name!");
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("alphanumeric")));
    }

    @Test
    void validate_returnsError_whenStagesAreMissing() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.setStages(null);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("at least one stage")));
    }

    @Test
    void validate_returnsError_whenStagesAreEmpty() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.setStages(new ArrayList<>());
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("at least one stage")));
    }

    @Test
    void validate_returnsError_whenStageNameIsMissing() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setName(null);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Stage name is required")));
    }

    @Test
    void validate_returnsError_whenStageTypeIsMissing() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setType(null);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Stage type is required")));
    }

    @Test
    void validate_returnsError_whenStageTypeIsInvalid() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setType("invalid-type");
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Invalid stage type")));
    }

    @Test
    void validate_returnsError_whenDuplicateStageName() {
        PipelineDefinition pipeline = createValidPipeline();
        StageDefinition duplicateStage = new StageDefinition();
        duplicateStage.setName("build");
        duplicateStage.setType("build");
        List<StageDefinition> stages = new ArrayList<>(pipeline.getStages());
        stages.add(duplicateStage);
        pipeline.setStages(stages);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Duplicate stage name")));
    }

    @Test
    void validate_returnsError_whenDependencyReferencesUnknownStage() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setDependsOn(List.of("non-existent-stage"));
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("depends on unknown stage")));
    }

    @Test
    void validate_returnsError_whenStageDependsOnItself() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setDependsOn(List.of("build"));
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("cannot depend on itself")));
    }

    @Test
    void validate_returnsError_whenStageTimeoutIsNegative() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setTimeout(-1);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("timeout must be positive")));
    }

    @Test
    void validate_returnsError_whenStageRetryIsNegative() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setRetry(-1);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("retry count cannot be negative")));
    }

    @Test
    void validate_returnsError_whenDeployTargetIsInvalid() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setType("deploy");
        pipeline.getStages().get(0).setTarget("invalid-target");
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Invalid deployment target")));
    }

    @Test
    void validate_returnsValid_whenDeployTargetIsValid() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setType("deploy");
        pipeline.getStages().get(0).setTarget("kubernetes");
        
        ValidationResult result = validator.validate(pipeline);
        
        assertTrue(result.isValid());
    }

    @Test
    void validate_returnsValid_forPluginStageTypes() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.getStages().get(0).setType("plugin:security-scan");
        
        ValidationResult result = validator.validate(pipeline);
        
        assertTrue(result.isValid());
    }

    @Test
    void validate_returnsValid_whenTimeoutIsPositive() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.setTimeout(3600);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertTrue(result.isValid());
    }

    @Test
    void validate_returnsError_whenPipelineTimeoutIsNegative() {
        PipelineDefinition pipeline = createValidPipeline();
        pipeline.setTimeout(-100);
        
        ValidationResult result = validator.validate(pipeline);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Timeout must be positive")));
    }

    private PipelineDefinition createValidPipeline() {
        PipelineDefinition pipeline = new PipelineDefinition();
        pipeline.setName("test-pipeline");
        
        StageDefinition stage = new StageDefinition();
        stage.setName("build");
        stage.setType("build");
        
        pipeline.setStages(List.of(stage));
        
        return pipeline;
    }
}
