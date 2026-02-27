package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.orchestrator.PipelineOrchestrator;
import com.hsbc.ci.engine.core.model.PipelineResult;

import java.util.Collection;

@Command(name = "pipeline", description = "Manage pipelines")
@Component
public class PipelineCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PipelineCommand.class);

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

    @Option(names = {"-n", "--name"}, description = "Pipeline name")
    private String name;

    @Option(names = {"-e", "--env"}, description = "Target environment")
    private String env;

    @Option(names = {"-v", "--var"}, description = "Variables (key=value)")
    private String[] vars;

    @Option(names = {"--dry-run"}, description = "Dry run mode")
    private boolean dryRun;

    @Override
    public void run() {
        if (list) {
            doList();
        } else if (run) {
            doRun();
        } else if (validate) {
            doValidate();
        } else {
            System.out.println("Use: pipeline --list | pipeline --run --name <name> | pipeline --validate --name <name>");
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
            System.exit(1);
        }

        try {
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
                    }
                }
            }

            PipelineResult result = pipelineOrchestrator.execute(context);

            if (result.isSuccess()) {
                System.out.println("[SUCCESS] Pipeline completed: " + name);
            } else {
                System.out.println("[FAILED] Pipeline failed: " + result.getError());
                System.exit(1);
            }
        } catch (Exception e) {
            log.error("Pipeline execution failed: {}", e.getMessage());
            System.err.println("[ERROR] Pipeline failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void doValidate() {
        if (name == null) {
            System.out.println("Error: --name required");
            System.exit(1);
        }

        log.info("Validating pipeline: {}", name);

        var pipeline = configurationLoader.getPipeline(name);

        if (pipeline == null) {
            System.out.println("[ERROR] Pipeline not found: " + name);
            System.exit(1);
        }

        System.out.println("[SUCCESS] Pipeline is valid: " + name);

        if (pipeline.containsKey("stages")) {
            var stages = (java.util.List<?>) pipeline.get("stages");
            System.out.println("  Stages: " + stages.size());
        }

        log.info("Pipeline validation passed: {}", name);
    }
}
