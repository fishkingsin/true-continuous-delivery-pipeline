package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import com.hsbc.ci.engine.core.stages.DeployStage;
import com.hsbc.ci.engine.core.model.PipelineContext;

import java.util.HashMap;
import java.util.Map;

@Command(name = "deploy", description = "Manage deployments")
@Component
public class DeployCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DeployCommand.class);

    @Autowired
    private DeployStage deployStage;

    @Option(names = {"-t", "--type"}, description = "Deployment type (kubernetes, ecs)", required = true)
    private String type;

    @Option(names = {"-n", "--namespace"}, description = "Kubernetes namespace")
    private String namespace = "default";

    @Option(names = {"-i", "--image"}, description = "Container image")
    private String image = "myapp:latest";

    @Option(names = {"-c", "--cluster"}, description = "ECS cluster")
    private String cluster = "default";

    @Option(names = {"--dry-run"}, description = "Dry run mode")
    private boolean dryRun;

    @Override
    public void run() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("type", type);
            config.put("namespace", namespace);
            config.put("image", image);
            config.put("cluster", cluster);

            log.info("Executing deployment: type={}, namespace={}, image={}, cluster={}", 
                type, namespace, image, cluster);

            String result = deployStage.execute(config, PipelineContext.builder()
                .pipelineName("cli-deploy")
                .build());

            System.out.println(result);
            log.info("Deployment completed successfully");

        } catch (Exception e) {
            log.error("Deployment failed: {}", e.getMessage());
            System.err.println("[ERROR] Deployment failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
