package com.hsbc.ci.engine.core.stages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Component
public class BuildStage implements Stage {

    private static final Logger log = LoggerFactory.getLogger(BuildStage.class);

    private static final String BUILDTOOL_MAVEN = "maven";
    private static final String BUILDTOOL_GRADLE = "gradle";
    private static final String BUILDTOOL_NPM = "npm";
    private static final String BUILDTOOL_DOTNET = "dotnet";
    private static final String BUILDTOOL_MAKE = "make";

    private static final Set<String> SUPPORTED_BUILD_TOOLS = Set.of(
        BUILDTOOL_MAVEN, "mvn",
        BUILDTOOL_GRADLE,
        BUILDTOOL_NPM, "node",
        BUILDTOOL_DOTNET,
        BUILDTOOL_MAKE
    );

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String buildTool = normalizeBuildTool((String) config.getOrDefault("build-tool", "maven"));
        String workingDir = resolveWorkingDir(config, context);
        
        log.info("Building with: {} in: {}", buildTool, workingDir);
        
        try {
            int exitCode = executeBuild(buildTool, workingDir, config);
            
            if (exitCode != 0) {
                log.error("Build failed with exit code: {}", exitCode);
                throw new RuntimeException("Build failed with exit code: " + exitCode);
            }
            
            log.info("Build completed successfully with {}", buildTool);
            return "Build completed successfully using " + buildTool;
            
        } catch (Exception e) {
            log.error("Build failed: {}", e.getMessage());
            throw new RuntimeException("Build failed: " + e.getMessage());
        }
    }

    private String normalizeBuildTool(String tool) {
        return switch (tool.toLowerCase()) {
            case "mvn" -> BUILDTOOL_MAVEN;
            case "node" -> BUILDTOOL_NPM;
            default -> tool.toLowerCase();
        };
    }

    private String resolveWorkingDir(Map<String, Object> config, PipelineContext context) {
        String workingDir = (String) config.getOrDefault("working-dir", ".");
        String contextDir = context.getVariable("WORKING_DIRECTORY");
        if (contextDir != null && !contextDir.isEmpty()) {
            return contextDir;
        }
        return workingDir;
    }

    private int executeBuild(String buildTool, String workingDir, Map<String, Object> config) throws Exception {
        return switch (buildTool) {
            case BUILDTOOL_MAVEN -> runMaven(workingDir, config);
            case BUILDTOOL_GRADLE -> runGradle(workingDir, config);
            case BUILDTOOL_NPM -> runNpm(workingDir, config);
            case BUILDTOOL_DOTNET -> runDotnet(workingDir, config);
            case BUILDTOOL_MAKE -> runMake(workingDir, config);
            default -> throw new RuntimeException("Unsupported build tool: " + buildTool);
        };
    }

    private int runMaven(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = buildCommand("mvn", config, 
            () -> ((String) config.getOrDefault("goals", "clean package")).split(" "),
            () -> {
                List<String> opts = new ArrayList<>();
                if (Boolean.TRUE.equals(config.getOrDefault("skipTests", true))) {
                    opts.add("-DskipTests");
                }
                return opts;
            });
        return runProcess(args, workingDir);
    }

    private int runGradle(String workingDir, Map<String, Object> config) throws Exception {
        Path gradlew = Paths.get(workingDir, "gradlew");
        String gradleCmd = Files.exists(gradlew) ? "./gradlew" : "gradle";
        
        List<String> args = buildCommand(gradleCmd, config,
            () -> ((String) config.getOrDefault("tasks", "build")).split(" "),
            () -> {
                List<String> opts = new ArrayList<>();
                if (Boolean.TRUE.equals(config.getOrDefault("skipTests", true))) {
                    opts.add("-x");
                    opts.add("test");
                }
                return opts;
            });
        return runProcess(args, workingDir);
    }

    private int runNpm(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = buildCommand("npm", config,
            () -> ((String) config.getOrDefault("command", "run build")).split(" "),
            ArrayList::new);
        return runProcess(args, workingDir);
    }

    private int runDotnet(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = buildCommand("dotnet", config,
            () -> ((String) config.getOrDefault("command", "build")).split(" "),
            ArrayList::new);
        return runProcess(args, workingDir);
    }

    private int runMake(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = buildCommand("make", config,
            () -> new String[]{(String) config.getOrDefault("target", "all")},
            ArrayList::new);
        return runProcess(args, workingDir);
    }

    @FunctionalInterface
    private interface CommandExtractor {
        String[] extract();
    }

    @FunctionalInterface
    private interface OptionsExtractor {
        List<String> extract();
    }

    private List<String> buildCommand(String baseCmd, Map<String, Object> config,
                                       CommandExtractor commandExtractor,
                                       OptionsExtractor optionsExtractor) {
        List<String> args = new ArrayList<>();
        args.add(baseCmd);
        args.addAll(Arrays.asList(commandExtractor.extract()));
        args.addAll(optionsExtractor.extract());
        
        String options = (String) config.get("options");
        if (options != null && !options.isBlank()) {
            args.addAll(Arrays.asList(options.split(" ")));
        }
        
        return args;
    }

    private int runProcess(List<String> args, String workingDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(new File(workingDir));
        pb.redirectErrorStream(true);
        
        log.debug("Running: {}", String.join(" ", args));
        
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("  {}", line);
            }
        }
        
        return process.waitFor();
    }
}
