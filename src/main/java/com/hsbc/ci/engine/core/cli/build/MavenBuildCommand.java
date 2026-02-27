package com.hsbc.ci.engine.core.cli.build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "maven", description = "Build with Maven")
@Component
public class MavenBuildCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MavenBuildCommand.class);

    @CommandLine.Option(names = {"-p", "--pom"}, description = "Path to pom.xml")
    private String pomPath = "pom.xml";

    @CommandLine.Option(names = {"-g", "--goals"}, description = "Maven goals", 
                        defaultValue = "clean package")
    private String goals = "clean package";

    @CommandLine.Option(names = {"-s", "--settings"}, description = "Path to settings.xml")
    private String settings;

    @CommandLine.Option(names = {"-P", "--profiles"}, description = "Maven profiles")
    private String profiles;

    @CommandLine.Option(names = {"--skip-tests"}, description = "Skip tests")
    private boolean skipTests;

    @CommandLine.Option(names = {"--parallel"}, description = "Enable parallel builds")
    private boolean parallel;

    @CommandLine.Option(names = {"-D"}, description = "Maven properties (-Dkey=value)")
    private String[] properties;

    @CommandLine.Option(names = {"-t", "--threads"}, description = "Build threads")
    private String threads;

    @CommandLine.Option(names = {"-o", "--offline"}, description = "Work offline")
    private boolean offline;

    @CommandLine.Option(names = {"-c", "--config"}, description = "Config YAML file")
    private String configFile;

    @CommandLine.Option(names = {"-v", "--var"}, description = "Variables (key=value)")
    private String[] variables;

    @Override
    public void run() {
        try {
            if (configFile != null) {
                buildFromConfig(configFile);
            } else {
                buildMaven();
            }
        } catch (Exception e) {
            log.error("Build failed: {}", e.getMessage());
            System.err.println("[ERROR] Build failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void buildFromConfig(String configFile) throws Exception {
        log.info("Loading build config from: {}", configFile);
        System.out.println("[INFO] Loading build config from: " + configFile);
        
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        java.nio.file.Path path = java.nio.file.Paths.get(configFile);
        if (!java.nio.file.Files.exists(path)) {
            throw new java.io.FileNotFoundException("Config file not found: " + configFile);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> config = yaml.load(java.nio.file.Files.readString(path));
        Map<String, Object> build = (Map<String, Object>) config.get("build");
        
        if (build == null) {
            throw new IllegalArgumentException("No 'build' section found in config");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> projects = (List<Map<String, Object>>) build.get("projects");
        
        if (projects == null || projects.isEmpty()) {
            log.info("No projects defined, running default build");
            System.out.println("[INFO] No projects defined, running default build");
            buildMaven();
            return;
        }

        Map<String, Object> defaults = (Map<String, Object>) build.get("defaults");
        Map<String, Object> mavenDefaults = (Map<String, Object>) ((Map<String, Object>) build.get("tools")).get("maven");

        for (Map<String, Object> project : projects) {
            String projectName = (String) project.get("name");
            String tool = (String) project.getOrDefault("tool", "maven");
            
            if (!"maven".equals(tool)) {
                log.info("Skipping {} (not maven)", projectName);
                System.out.println("[SKIP] Skipping " + projectName + " (not maven)");
                continue;
            }

            log.info("Building project: {}", projectName);
            System.out.println("[INFO] Building project: " + projectName);
            
            String projPom = (String) project.getOrDefault("pom", "pom.xml");
            String projGoals = (String) project.getOrDefault("goals", goals);
            Boolean projSkipTests = (Boolean) project.getOrDefault("skipTests", skipTests);
            Boolean projParallel = (Boolean) project.getOrDefault("parallel", parallel);
            
            buildMavenProject(projPom, projGoals, projSkipTests, projParallel);
        }
    }

    private void buildMaven() throws Exception {
        buildMavenProject(pomPath, goals, skipTests, parallel);
    }

    private void buildMavenProject(String pom, String goals, boolean skipTests, boolean parallel) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("mvn");

        if (settings != null) {
            cmd.add("-s");
            cmd.add(settings);
        }

        if (profiles != null) {
            cmd.add("-P");
            cmd.add(profiles);
        }

        if (skipTests) {
            cmd.add("-DskipTests");
        }

        if (parallel) {
            String threadArg = threads != null ? threads : "2C";
            cmd.add("-T");
            cmd.add(threadArg);
        }

        if (offline) {
            cmd.add("-o");
        }

        if (properties != null) {
            for (String prop : properties) {
                cmd.add("-D" + prop);
            }
        }

        for (String goal : goals.split("\\s+")) {
            if (!goal.isEmpty()) {
                cmd.add(goal);
            }
        }

        cmd.add("-f");
        cmd.add(pom);

        log.debug("Running: {}", String.join(" ", cmd));
        System.out.println("[INFO] Running: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        
        String workDir = new File(pom).getParent();
        if (workDir != null) {
            pb.directory(new File(workDir));
        }

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Maven build failed with exit code: " + exitCode);
        }

        log.info("Build completed successfully");
        System.out.println("[SUCCESS] Build completed successfully");
    }
}
