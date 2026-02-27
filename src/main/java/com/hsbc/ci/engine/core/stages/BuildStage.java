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

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String buildTool = (String) config.getOrDefault("build-tool", "maven");
        String workingDir = (String) config.getOrDefault("working-dir", ".");
        
        String contextDir = context.getVariable("WORKING_DIRECTORY");
        if (contextDir != null && !contextDir.isEmpty()) {
            workingDir = contextDir;
        }
        
        System.out.println("  Building with: " + buildTool + " in: " + workingDir);
        
        try {
            int exitCode;
            
            switch (buildTool.toLowerCase()) {
                case "maven":
                case "mvn":
                    exitCode = runMaven(workingDir, config);
                    break;
                case "gradle":
                    exitCode = runGradle(workingDir, config);
                    break;
                case "npm":
                case "node":
                    exitCode = runNpm(workingDir, config);
                    break;
                case "dotnet":
                    exitCode = runDotnet(workingDir, config);
                    break;
                case "make":
                    exitCode = runMake(workingDir, config);
                    break;
                default:
                    throw new RuntimeException("Unsupported build tool: " + buildTool);
            }
            
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

    private int runMaven(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = new ArrayList<>();
        args.add("mvn");
        
        String goals = (String) config.getOrDefault("goals", "clean package");
        args.addAll(Arrays.asList(goals.split(" ")));
        
        Boolean skipTests = (Boolean) config.getOrDefault("skipTests", true);
        if (skipTests) {
            args.add("-DskipTests");
        }
        
        String options = (String) config.get("options");
        if (options != null && !options.isEmpty()) {
            args.addAll(Arrays.asList(options.split(" ")));
        }
        
        return runProcess(args, workingDir);
    }

    private int runGradle(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = new ArrayList<>();
        
        Path gradlew = Paths.get(workingDir, "gradlew");
        if (Files.exists(gradlew)) {
            args.add("./gradlew");
        } else {
            args.add("gradle");
        }
        
        String tasks = (String) config.getOrDefault("tasks", "build");
        args.addAll(Arrays.asList(tasks.split(" ")));
        
        Boolean skipTests = (Boolean) config.getOrDefault("skipTests", true);
        if (skipTests) {
            args.add("-x");
            args.add("test");
        }
        
        String options = (String) config.get("options");
        if (options != null && !options.isEmpty()) {
            args.addAll(Arrays.asList(options.split(" ")));
        }
        
        return runProcess(args, workingDir);
    }

    private int runNpm(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = new ArrayList<>();
        args.add("npm");
        
        String command = (String) config.getOrDefault("command", "run build");
        args.addAll(Arrays.asList(command.split(" ")));
        
        String options = (String) config.get("options");
        if (options != null && !options.isEmpty()) {
            args.addAll(Arrays.asList(options.split(" ")));
        }
        
        return runProcess(args, workingDir);
    }

    private int runDotnet(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = new ArrayList<>();
        args.add("dotnet");
        
        String command = (String) config.getOrDefault("command", "build");
        args.addAll(Arrays.asList(command.split(" ")));
        
        String options = (String) config.get("options");
        if (options != null && !options.isEmpty()) {
            args.addAll(Arrays.asList(options.split(" ")));
        }
        
        return runProcess(args, workingDir);
    }

    private int runMake(String workingDir, Map<String, Object> config) throws Exception {
        List<String> args = new ArrayList<>();
        args.add("make");
        
        String target = (String) config.getOrDefault("target", "all");
        args.add(target);
        
        String options = (String) config.get("options");
        if (options != null && !options.isEmpty()) {
            args.addAll(Arrays.asList(options.split(" ")));
        }
        
        return runProcess(args, workingDir);
    }

    private int runProcess(List<String> args, String workingDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(new File(workingDir));
        pb.redirectErrorStream(true);
        
        log.info("Running: {}", String.join(" ", args));
        
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
        }
        
        return process.waitFor();
    }
}
