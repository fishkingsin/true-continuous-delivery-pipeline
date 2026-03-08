package com.hsbc.ci.engine.core.cli.build;

import com.hsbc.ci.engine.core.utils.ConsoleOutput;
import com.hsbc.ci.engine.core.utils.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "maven", description = "Build with Maven")
@Component
public class MavenBuildCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MavenBuildCommand.class);

    @Autowired
    private ConsoleOutput console;

    @Autowired
    private ProcessExecutor processExecutor;

    @CommandLine.Option(names = {"-p", "--pom"}, description = "Path to pom.xml")
    private String pomPath = "pom.xml";

    @CommandLine.Option(names = {"-g", "--goals"}, description = "Maven goals", 
                        defaultValue = "clean package")
    private String goals = "clean package";

    @CommandLine.Option(names = {"-s", "--settings"}, description = "Path to settings.xml")
    private String settings;

    @CommandLine.Option(names = {"--skip-tests"}, description = "Skip tests")
    private boolean skipTests;

    @CommandLine.Option(names = {"--parallel"}, description = "Enable parallel builds")
    private boolean parallel;

    @CommandLine.Option(names = {"-c", "--config"}, description = "Config YAML file")
    private String configFile;

    @Override
    public void run() {
        try {
            if (configFile != null) {
                runFromConfig();
            } else {
                runMaven();
            }
        } catch (Exception e) {
            log.error("Build failed: {}", e.getMessage());
            console.printError("[ERROR] Build failed: " + e.getMessage());
        }
    }

    private void runFromConfig() throws Exception {
        console.print("[INFO] Loading build config: " + configFile);
        Map<String, Object> config = loadConfig(configFile);
        List<Map<String, Object>> projects = extractProjects(config);
        
        if (projects == null || projects.isEmpty()) {
            console.print("[INFO] No projects, running default");
            runMaven();
            return;
        }
        
        for (var project : projects) {
            buildProject(project);
        }
    }

    private Map<String, Object> loadConfig(String file) throws Exception {
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Config not found: " + file);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> config = new org.yaml.snakeyaml.Yaml()
            .load(Files.readString(path));
        return config;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractProjects(Map<String, Object> config) {
        var build = (Map<String, Object>) config.get("build");
        if (build == null) return null;
        return (List<Map<String, Object>>) build.get("projects");
    }

    private void buildProject(Map<String, Object> project) throws Exception {
        String tool = (String) project.getOrDefault("tool", "maven");
        if (!"maven".equals(tool)) {
            console.print("[SKIP] Not maven: " + project.get("name"));
            return;
        }
        
        console.print("[INFO] Building: " + project.get("name"));
        runMavenProject(
            (String) project.getOrDefault("pom", "pom.xml"),
            (String) project.getOrDefault("goals", goals)
        );
    }

    private void runMaven() throws Exception {
        runMavenProject(pomPath, goals);
    }

    private void runMavenProject(String pom, String goals) throws Exception {
        List<String> cmd = buildCommand(pom, goals);
        console.print("[INFO] Running: " + String.join(" ", cmd));
        
        String dir = new File(pom).getParent();
        int exitCode = processExecutor.execute(cmd, dir != null ? dir : ".");
        
        if (exitCode != 0) {
            throw new RuntimeException("Maven failed: " + exitCode);
        }
        console.print("[SUCCESS] Build completed");
    }

    private List<String> buildCommand(String pom, String goals) {
        List<String> cmd = new ArrayList<>();
        cmd.add("mvn");
        
        if (settings != null) {
            cmd.add("-s");
            cmd.add(settings);
        }
        if (skipTests) {
            cmd.add("-DskipTests");
        }
        if (parallel) {
            cmd.add("-T2C");
        }
        
        cmd.addAll(List.of(goals.split(" ")));
        return cmd;
    }
}
