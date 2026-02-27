package com.hsbc.ci.engine.core.config;

import com.hsbc.ci.engine.core.model.PipelineDefinition;
import com.hsbc.ci.engine.core.model.StageDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class PipelineValidator {

    private static final Set<String> VALID_STAGE_TYPES = Set.of(
            "build", "test", "deploy", "containerize", "checkout",
            "plugin:security-scan", "plugin:sonarqube", "plugin:performance-test", "plugin:chaos-engineering"
    );

    private static final Set<String> VALID_DEPLOY_TARGETS = Set.of("kubernetes", "ecs", "local");

    public ValidationResult validate(PipelineDefinition pipeline) {
        List<String> errors = new ArrayList<>();

        if (pipeline.getName() == null || pipeline.getName().isBlank()) {
            errors.add("Pipeline name is required");
        } else if (!pipeline.getName().matches("^[a-zA-Z0-9_-]+$")) {
            errors.add("Pipeline name must be alphanumeric with hyphens or underscores");
        }

        if (pipeline.getStages() == null || pipeline.getStages().isEmpty()) {
            errors.add("Pipeline must have at least one stage");
        } else {
            validateStages(pipeline.getStages(), errors);
        }

        if (pipeline.getTimeout() != null && pipeline.getTimeout() <= 0) {
            errors.add("Timeout must be positive if specified");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private void validateStages(List<StageDefinition> stages, List<String> errors) {
        Set<String> stageNames = new HashSet<>();
        Set<String> validTypes = getValidTypes();

        for (StageDefinition stage : stages) {
            if (stage.getName() == null || stage.getName().isBlank()) {
                errors.add("Stage name is required");
                continue;
            }

            if (!stageNames.add(stage.getName())) {
                errors.add("Duplicate stage name: " + stage.getName());
            }

            if (stage.getType() == null || stage.getType().isBlank()) {
                errors.add("Stage type is required for stage: " + stage.getName());
            } else if (!validTypes.contains(stage.getType())) {
                errors.add("Invalid stage type: " + stage.getType() + " for stage: " + stage.getName());
            }

            if (stage.getTimeout() != null && stage.getTimeout() <= 0) {
                errors.add("Stage timeout must be positive for stage: " + stage.getName());
            }

            if (stage.getRetry() != null && stage.getRetry() < 0) {
                errors.add("Stage retry count cannot be negative for stage: " + stage.getName());
            }

            if (stage.getTarget() != null && !VALID_DEPLOY_TARGETS.contains(stage.getTarget())) {
                errors.add("Invalid deployment target: " + stage.getTarget() + " for stage: " + stage.getName());
            }

            validateDependencies(stage, stages, errors);
        }
    }

    private void validateDependencies(StageDefinition stage, List<StageDefinition> allStages, List<String> errors) {
        if (stage.getDependsOn() == null) {
            return;
        }

        Set<String> availableStageNames = new HashSet<>();
        for (StageDefinition s : allStages) {
            availableStageNames.add(s.getName());
        }

        for (String dep : stage.getDependsOn()) {
            if (!availableStageNames.contains(dep)) {
                errors.add("Stage '" + stage.getName() + "' depends on unknown stage: " + dep);
            }
        }

        if (stage.getDependsOn().contains(stage.getName())) {
            errors.add("Stage '" + stage.getName() + "' cannot depend on itself");
        }
    }

    private Set<String> getValidTypes() {
        return VALID_STAGE_TYPES;
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
