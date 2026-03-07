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
import com.hsbc.ci.engine.core.config.PipelineValidator;

import java.util.Collection;

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

    @Autowired
    private ConsoleOutput console;

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
                console.print("Use: pipeline --list | pipeline --run --name <name> | pipeline --validate --name <name> | pipeline --status --name <name>");
            }
        } catch (IllegalArgumentException e) {
            console.printError("[ERROR] " + e.getMessage());
            exitCode = EXIT_INVALID_DEFINITION;
        } catch (PipelineExecutionException e) {
            console.printError("[ERROR] " + e.getMessage());
            exitCode = EXIT_FAILURE;
        }
    }

    private void doList() {
        Collection<String> pipelines = configurationLoader.listPipelines();

        console.print("Available Pipelines:");
        console.print("");

        if (pipelines.isEmpty()) {
            console.print("(No pipelines found. Add pipeline configs to config/pipelines/)");
        } else {
            for (String pipeline : pipelines) {
                console.print("  " + pipeline);
            }
        }

        log.debug("Listed {} pipelines", pipelines.size());
    }

    private void doRun() {
        validatePipelineName();
        
        try {
            if (verbose) {
                console.print(">>> Starting pipeline: " + name);
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
                            console.print("    Variable: " + parts[0] + "=" + parts[1]);
                        }
                    }
                }
            }

            if (verbose) {
                console.print(">>> Executing pipeline...");
            }

            PipelineResult result = pipelineOrchestrator.execute(context);
            String output = PipelineOutputFormatter.formatPipelineOutput(name, env, context.getStageResults(), !result.isSuccess());
            console.print(output);

            if (result.isSuccess()) {
                console.print("[SUCCESS] Pipeline completed: " + name);
                exitCode = EXIT_SUCCESS;
            } else {
                console.print("[FAILED] Pipeline failed: " + result.getError());
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
            console.printError("[ERROR] Invalid pipeline definition: " + message);
            exitCode = EXIT_INVALID_DEFINITION;
        } else {
            console.printError("[ERROR] Pipeline failed: " + message);
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
                console.print("[INFO] Validating: pipelines/" + name + ".yml");
                for (String error : validationResult.getErrors()) {
                    console.print("  ✗ " + error);
                }
                console.print("[FAILED] Configuration is invalid");
                exitCode = EXIT_INVALID_DEFINITION;
                return;
            }
            
            console.print("[INFO] Validating: pipelines/" + name + ".yml");
            console.print("[INFO] ✓ Pipeline syntax valid");
            console.print("[INFO] ✓ All referenced stages exist");
            console.print("[INFO] ✓ Environment references valid");
            console.print("[SUCCESS] Configuration is valid");

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

        console.print("Pipeline: " + name);
        
        if (pipeline.containsKey("description")) {
            console.print("Description: " + pipeline.get("description"));
        }
        
        if (pipeline.containsKey("stages")) {
            var stages = (java.util.List<?>) pipeline.get("stages");
            console.print("\nStages (" + stages.size() + "):");
            for (Object stageObj : stages) {
                var stage = (java.util.Map<String, Object>) stageObj;
                String stageName = (String) stage.get("name");
                String stageType = (String) stage.get("type");
                console.print("  - " + stageName + " (" + stageType + ")");
            }
        }

        if (pipeline.containsKey("environments")) {
            var envs = (java.util.List<?>) pipeline.get("environments");
            console.print("\nEnvironments: " + String.join(", ", envs.stream().map(Object::toString).toList()));
        }
    }

    public static class PipelineExecutionException extends RuntimeException {
        public PipelineExecutionException(String message) {
            super(message);
        }
    }
}
