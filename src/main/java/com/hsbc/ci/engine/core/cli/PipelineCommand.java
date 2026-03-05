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

    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_FAILURE = 1;
    public static final int EXIT_INVALID_DEFINITION = 2;

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private PipelineOrchestrator pipelineOrchestrator;

    @Autowired
    private PipelineValidator pipelineValidator;

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

    private int exitCode = EXIT_SUCCESS;

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public void run() {
        try {
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
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
            exitCode = EXIT_INVALID_DEFINITION;
        } catch (PipelineExecutionException e) {
            System.err.println("[ERROR] " + e.getMessage());
            exitCode = EXIT_FAILURE;
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
        validatePipelineName();
        
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
            String output = PipelineOutputFormatter.formatPipelineOutput(name, env, context.getStageResults(), !result.isSuccess());
            System.out.println(output);

            if (result.isSuccess()) {
                System.out.println("[SUCCESS] Pipeline completed: " + name);
                exitCode = EXIT_SUCCESS;
            } else {
                System.out.println("[FAILED] Pipeline failed: " + result.getError());
                exitCode = EXIT_FAILURE;
            }
        } catch (Exception e) {
            log.error("Pipeline execution failed: {}", e.getMessage());
            handleExecutionError(e);
        }
    }

    private void validatePipelineName() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("--name is required");
        }
    }

    private void handleExecutionError(Exception e) {
        String message = e.getMessage();
        if (message != null && message.contains("validation")) {
            System.err.println("[ERROR] Invalid pipeline definition: " + message);
            exitCode = EXIT_INVALID_DEFINITION;
        } else {
            System.err.println("[ERROR] Pipeline failed: " + message);
            exitCode = EXIT_FAILURE;
        }
    }

    private void doValidate() {
        validatePipelineName();
        
        log.info("Validating pipeline: {}", name);

        var pipeline = configurationLoader.getPipeline(name);

        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline not found: " + name);
        }

        try {
            PipelineDefinition def = configurationLoader.loadPipelineDefinition(name);
            PipelineValidator.ValidationResult validationResult = pipelineValidator.validate(def);
            
            if (!validationResult.isValid()) {
                System.out.println("[INFO] Validating: pipelines/" + name + ".yml");
                for (String error : validationResult.getErrors()) {
                    System.out.println("  ✗ " + error);
                }
                System.out.println("[FAILED] Configuration is invalid");
                exitCode = EXIT_INVALID_DEFINITION;
                return;
            }
            
            System.out.println("[INFO] Validating: pipelines/" + name + ".yml");
            System.out.println("[INFO] ✓ Pipeline syntax valid");
            System.out.println("[INFO] ✓ All referenced stages exist");
            System.out.println("[INFO] ✓ Environment references valid");
            System.out.println("[SUCCESS] Configuration is valid");

            log.info("Pipeline validation passed: {}", name);
        } catch (Exception e) {
            throw new PipelineExecutionException("Validation error: " + e.getMessage());
        }
    }

    private void doStatus() {
        validatePipelineName();
        
        var pipeline = configurationLoader.getPipeline(name);
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline not found: " + name);
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

    public static class PipelineExecutionException extends RuntimeException {
        public PipelineExecutionException(String message) {
            super(message);
        }
    }
}
