package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.model.PipelineDefinition;
import com.hsbc.ci.engine.core.orchestrator.PipelineOrchestrator;
import com.hsbc.ci.engine.core.model.PipelineResult;
import com.hsbc.ci.engine.core.model.StageResult;
import com.hsbc.ci.engine.core.config.PipelineValidator;

import java.util.Collection;
import java.util.Map;

@Command(name = "pipeline", description = "Manage pipelines")
@Component
public class PipelineCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PipelineCommand.class);

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_INVALID_DEFINITION = 2;

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private PipelineOrchestrator pipelineOrchestrator;

    @Option(names = {"--list"}, description = "List pipelines")
    private boolean list;

    @Option(names = {"--run"}, description = "Run pipeline")
    private boolean run;

    @Option(names = {"--validate"}, description = "Validate pipeline")
    private boolean validate;

    @Option(names = {"--status"}, description = "Show pipeline status")
    private boolean status;

    @Option(names = {"-n", "--name"}, description = "Pipeline name")
    private String name;

    @Option(names = {"-e", "--env"}, description = "Target environment")
    private String env;

    @Option(names = {"-v", "--var"}, description = "Variables (key=value)")
    private String[] vars;

    @Option(names = {"--dry-run"}, description = "Dry run mode")
    private boolean dryRun;

    @Option(names = {"--verbose"}, description = "Verbose output")
    private boolean verbose;

    @Override
    public void run() {
        if (list) {
            doList();
        } else if (run) {
            doRun();
        } else if (validate) {
            doValidate();
        } else if (status) {
            doStatus();
        } else {
            System.out.println("Use: pipeline --list | pipeline --run --name <name> | pipeline --validate --name <name> | pipeline --status --name <name>");
        }
    }

    private void doList() {
        Collection<String> pipelines = configurationLoader.listPipelines();

        System.out.println("Available Pipelines:");
        System.out.println("");

        if (pipelines.isEmpty()) {
            System.out.println("(No pipelines found. Add pipeline configs to config/pipelines/)");
        } else {
            for (String pipeline : pipelines) {
                System.out.println("  " + pipeline);
            }
        }

        log.debug("Listed {} pipelines", pipelines.size());
    }

    private void doRun() {
        if (name == null) {
            System.out.println("Error: --name required");
            System.exit(EXIT_FAILURE);
        }

        try {
            if (verbose) {
                System.out.println(">>> Starting pipeline: " + name);
            }
            log.info("Running pipeline: {}", name);

            PipelineContext context = PipelineContext.builder()
                .pipelineName(name)
                .environment(env)
                .dryRun(dryRun)
                .build();

            if (vars != null) {
                for (String var : vars) {
                    String[] parts = var.split("=", 2);
                    if (parts.length == 2) {
                        context.addVariable(parts[0], parts[1]);
                        if (verbose) {
                            System.out.println("    Variable: " + parts[0] + "=" + parts[1]);
                        }
                    }
                }
            }

            if (verbose) {
                System.out.println(">>> Executing pipeline...");
            }

            PipelineResult result = pipelineOrchestrator.execute(context);

            if (result.isSuccess()) {
                System.out.println("[SUCCESS] Pipeline completed: " + name);
                
                Map<String, StageResult> stageResults = context.getStageResults();
                if (verbose && !stageResults.isEmpty()) {
                    System.out.println("\nStage Results:");
                    for (Map.Entry<String, StageResult> entry : stageResults.entrySet()) {
                        StageResult sr = entry.getValue();
                        String status = sr.isSuccess() ? "✓" : "✗";
                        System.out.printf("  %s %s (%dms)%n", status, entry.getKey(), sr.getDurationMs());
                    }
                }
                
                System.exit(EXIT_SUCCESS);
            } else {
                System.out.println("[FAILED] Pipeline failed: " + result.getError());
                System.exit(EXIT_FAILURE);
            }
        } catch (Exception e) {
            log.error("Pipeline execution failed: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("validation")) {
                System.err.println("[ERROR] Invalid pipeline definition: " + e.getMessage());
                System.exit(EXIT_INVALID_DEFINITION);
            }
            System.err.println("[ERROR] Pipeline failed: " + e.getMessage());
            System.exit(EXIT_FAILURE);
        }
    }

    private void doValidate() {
        if (name == null) {
            System.out.println("Error: --name required");
            System.exit(EXIT_FAILURE);
        }

        log.info("Validating pipeline: {}", name);

        var pipeline = configurationLoader.getPipeline(name);

        if (pipeline == null) {
            System.out.println("[ERROR] Pipeline not found: " + name);
            System.exit(EXIT_INVALID_DEFINITION);
        }

        try {
            PipelineDefinition def = configurationLoader.loadPipelineDefinition(name);
            PipelineValidator validator = new PipelineValidator();
            PipelineValidator.ValidationResult validationResult = validator.validate(def);
            
            if (!validationResult.isValid()) {
                System.out.println("[ERROR] Pipeline validation failed:");
                for (String error : validationResult.getErrors()) {
                    System.out.println("  - " + error);
                }
                System.exit(EXIT_INVALID_DEFINITION);
            }
            
            System.out.println("[SUCCESS] Pipeline is valid: " + name);

            if (pipeline.containsKey("stages")) {
                var stages = (java.util.List<?>) pipeline.get("stages");
                System.out.println("  Stages: " + stages.size());
            }

            log.info("Pipeline validation passed: {}", name);
        } catch (Exception e) {
            System.out.println("[ERROR] Validation error: " + e.getMessage());
            System.exit(EXIT_INVALID_DEFINITION);
        }
    }

    private void doStatus() {
        if (name == null) {
            System.out.println("Error: --name required");
            System.exit(EXIT_FAILURE);
        }

        var pipeline = configurationLoader.getPipeline(name);
        if (pipeline == null) {
            System.out.println("[ERROR] Pipeline not found: " + name);
            System.exit(EXIT_FAILURE);
        }

        System.out.println("Pipeline: " + name);
        
        if (pipeline.containsKey("description")) {
            System.out.println("Description: " + pipeline.get("description"));
        }
        
        if (pipeline.containsKey("stages")) {
            var stages = (java.util.List<?>) pipeline.get("stages");
            System.out.println("\nStages (" + stages.size() + "):");
            for (Object stageObj : stages) {
                var stage = (java.util.Map<String, Object>) stageObj;
                String stageName = (String) stage.get("name");
                String stageType = (String) stage.get("type");
                System.out.println("  - " + stageName + " (" + stageType + ")");
            }
        }

        if (pipeline.containsKey("environments")) {
            var envs = (java.util.List<?>) pipeline.get("environments");
            System.out.println("\nEnvironments: " + String.join(", ", envs.stream().map(Object::toString).toList()));
        }
    }
}
