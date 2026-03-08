package com.hsbc.ci.engine.core.stages;

import com.hsbc.ci.engine.core.model.PipelineContext;
import com.hsbc.ci.engine.core.utils.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class BuildStage implements Stage {

    private static final Logger log = LoggerFactory.getLogger(BuildStage.class);
    private static final String MAVEN = "maven";
    private static final String GRADLE = "gradle";
    private static final String NPM = "npm";
    private static final String DOTNET = "dotnet";
    private static final String MAKE = "make";

    @Autowired
    private ProcessExecutor processExecutor;

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String tool = getTool(config);
        String dir = getWorkingDir(config, context);
        
        log.info("Building with: {} in: {}", tool, dir);
        
        try {
            int exitCode = processExecutor.execute(buildArgs(tool, config), dir);
            if (exitCode != 0) {
                throw new RuntimeException("Build failed: exit code " + exitCode);
            }
            return "Build completed using " + tool;
        } catch (Exception e) {
            log.error("Build failed: {}", e.getMessage());
            throw new RuntimeException("Build failed: " + e.getMessage());
        }
    }

    private String getTool(Map<String, Object> config) {
        return ((String) config.getOrDefault("build-tool", MAVEN)).toLowerCase();
    }

    private String getWorkingDir(Map<String, Object> config, PipelineContext ctx) {
        String ctxDir = ctx.getVariable("WORKING_DIRECTORY");
        return ctxDir != null ? ctxDir : (String) config.getOrDefault("working-dir", ".");
    }

    private List<String> buildArgs(String tool, Map<String, Object> config) {
        return switch (tool) {
            case MAVEN -> mavenArgs(config);
            case GRADLE -> gradleArgs(config);
            case NPM -> npmArgs(config);
            case DOTNET -> dotnetArgs(config);
            case MAKE -> makeArgs(config);
            default -> throw new IllegalArgumentException("Unsupported: " + tool);
        };
    }

    private List<String> mavenArgs(Map<String, Object> config) {
        List<String> args = new ArrayList<>();
        args.add("mvn");
        args.addAll(Arrays.asList(
            ((String) config.getOrDefault("goals", "clean package")).split(" ")));
        if (Boolean.TRUE.equals(config.getOrDefault("skipTests", true))) {
            args.add("-DskipTests");
        }
        return args;
    }

    private List<String> gradleArgs(Map<String, Object> config) {
        List<String> args = new ArrayList<>();
        args.add("gradlew");
        args.addAll(Arrays.asList(
            ((String) config.getOrDefault("tasks", "build")).split(" ")));
        if (Boolean.TRUE.equals(config.getOrDefault("skipTests", true))) {
            args.add("-x");
            args.add("test");
        }
        return args;
    }

    private List<String> npmArgs(Map<String, Object> config) {
        List<String> args = new ArrayList<>();
        args.add("npm");
        args.addAll(Arrays.asList(
            ((String) config.getOrDefault("command", "run build")).split(" ")));
        return args;
    }

    private List<String> dotnetArgs(Map<String, Object> config) {
        List<String> args = new ArrayList<>();
        args.add("dotnet");
        args.addAll(Arrays.asList(
            ((String) config.getOrDefault("command", "build")).split(" ")));
        return args;
    }

    private List<String> makeArgs(Map<String, Object> config) {
        List<String> args = new ArrayList<>();
        args.add("make");
        args.add((String) config.getOrDefault("target", "all"));
        return args;
    }
}
