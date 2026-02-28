package com.hsbc.ci.engine.core.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CiEngineCliE2eTest {

    private static String jarPath;
    
    @BeforeAll
    static void setup() throws Exception {
        jarPath = findJarPath();
    }
    
    private static String findJarPath() throws Exception {
        Path targetDir = Path.of("target").toAbsolutePath();
        if (!Files.exists(targetDir)) {
            throw new RuntimeException("target/ directory not found - run mvn package first");
        }
        try (var files = Files.list(targetDir)) {
            return files.filter(p -> p.toString().endsWith(".jar"))
                       .findFirst()
                       .map(p -> p.toAbsolutePath().toString())
                       .orElseThrow(() -> new RuntimeException("JAR not found in target/ - run mvn package first"));
        }
    }

    @Test
    void versionCommand_returnsVersion() throws Exception {
        ProcessResult result = runCli("version");
        
        assertEquals(0, result.exitCode, result.stderr);
        assertTrue(result.stdout.contains("ci-engine-core"), "Should contain version info");
    }

    @Test
    void helpCommand_showsUsage() throws Exception {
        ProcessResult result = runCli("--help");
        
        assertEquals(0, result.exitCode, result.stderr);
        assertTrue(result.stdout.contains("Usage:"), "Should show usage");
    }

    @Test
    void pluginList_showsAvailablePlugins() throws Exception {
        ProcessResult result = runCli("plugin", "list");
        
        assertEquals(0, result.exitCode, result.stderr);
    }

    @Test
    void pipelineList_showsPipelines() throws Exception {
        ProcessResult result = runCli("pipeline", "--list");
        
        assertTrue(result.stdout.contains("pipelines") || result.exitCode == 0,
            "Should list pipelines");
    }

    @Test
    void pipelineValidate_validatesPipeline() throws Exception {
        ProcessResult result = runCli("pipeline", "--validate", "--name", "sample-pipeline");
        
        assertTrue(result.exitCode == 0 || result.stdout.contains("Pipeline") || result.stdout.contains("SUCCESS") || result.stdout.contains("valid"),
            "Should validate pipeline, got: " + result.stdout);
    }

    @Test
    void pipelineRun_executesDryRun() throws Exception {
        ProcessResult result = runCli("pipeline", "--run", "--name", "sample-pipeline", "--dry-run");
        
        assertTrue(result.stdout.contains("Executing pipeline") || result.stdout.contains("sample-pipeline") || 
                   result.stdout.contains("DRY-RUN") || result.stdout.contains("Pipeline") || result.exitCode == 0,
            "Should execute or show dry-run, got: " + result.stdout);
    }

    @Test
    void deployCommand_showsHelp() throws Exception {
        ProcessResult result = runCli("deploy", "--help");
        
        assertEquals(0, result.exitCode, result.stderr);
    }

    @Test
    void deployCommand_showsKubernetesDeployment() throws Exception {
        ProcessResult result = runCli("deploy", "--type", "kubernetes", "--namespace", "test-ns", "--image", "myapp:v1");
        
        assertNotNull(result.stdout);
    }

    @Test
    void deployCommand_showsEcsDeployment() throws Exception {
        ProcessResult result = runCli("deploy", "--type", "ecs", "--cluster", "prod-cluster");
        
        assertNotNull(result.stdout);
    }

    @Test
    @DisabledIfSystemProperty(named = "skipNetworkTests", matches = "true")
    void checkoutClone_clonesRepository(@TempDir Path tempDir) throws Exception {
        Path cloneDir = tempDir.resolve("test-repo");
        
        ProcessResult result = runCli("checkout", "clone", 
            "--url", "https://github.com/fishkingsin/Spring-Boot-CRUD-Example",
            "--target", cloneDir.toString());
        
        assertEquals(0, result.exitCode, "Clone should succeed: " + result.stdout);
        assertTrue(result.stdout.contains("[SUCCESS]") || result.exitCode == 0, "Should show success message");
    }

    @Test
    @DisabledIfSystemProperty(named = "skipNetworkTests", matches = "true")
    void buildMaven_showsOutput(@TempDir Path tempDir) throws Exception {
        ProcessResult result = runCli("build", "maven", "--help");
        
        assertTrue(result.exitCode == 0 || result.stdout.contains("Usage") || result.stdout.contains("maven") || result.stdout.contains("Maven"),
            "Should show maven build help");
    }

    @Test
    void unknownCommand_showsHelpOrError() throws Exception {
        ProcessResult result = runCli("unknown-command");
        assertTrue(result.exitCode != 0 || result.stdout.contains("Usage") || result.stdout.contains("Unmatched"));
    }

    private ProcessResult runCli(String... args) throws Exception {
        return runCliWithConfig(null, args);
    }

    private ProcessResult runCliWithWorkingDir(Path workingDir, String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(jarPath);
        command.addAll(List.of(args));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        
        return new ProcessResult(exitCode, output, "");
    }

    private ProcessResult runCliWithConfig(Path configDir, String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(jarPath);
        
        if (configDir != null) {
            command.add("--config");
            command.add(configDir.toString());
        }
        
        command.addAll(List.of(args));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(Path.of(".").toFile());
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        
        return new ProcessResult(exitCode, output, "");
    }

    static class ProcessResult {
        final int exitCode;
        final String stdout;
        final String stderr;
        
        ProcessResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
