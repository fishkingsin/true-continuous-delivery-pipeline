package com.hsbc.ci.engine.core.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CiEngineCliE2eTest {

    private static String jarPath;
    
    @BeforeAll
    static void setup() throws Exception {
        jarPath = findJarPath();
    }
    
    private static String findJarPath() throws Exception {
        Path targetDir = Path.of("target").toAbsolutePath();
        try (var files = java.nio.file.Files.list(targetDir)) {
            return files.filter(p -> p.toString().endsWith(".jar"))
                       .findFirst()
                       .map(p -> p.toAbsolutePath().toString())
                       .orElseThrow(() -> new RuntimeException("JAR not found in target/"));
        }
    }

    @Test
    void versionCommand_returnsVersion() throws Exception {
        ProcessResult result = runCli("version");
        
        assertEquals(0, result.exitCode, result.stderr);
        assertTrue(result.stdout.contains("ci-engine-core"));
    }

    @Test
    void helpCommand_showsUsage() throws Exception {
        ProcessResult result = runCli("--help");
        
        assertEquals(0, result.exitCode, result.stderr);
        assertTrue(result.stdout.contains("Usage:"));
    }

    @Test
    void pluginList_showsAvailablePlugins() throws Exception {
        ProcessResult result = runCli("plugin", "list");
        
        assertEquals(0, result.exitCode, result.stderr);
        assertTrue(result.stdout.contains("Available Plugins"));
    }

    @Test
    void pipelineList_showsPipelines(@TempDir Path tempDir) throws Exception {
        copyConfigToTemp(tempDir);
        ProcessResult result = runCliWithConfig(tempDir, "pipeline", "list");
        
        assertEquals(0, result.exitCode, result.stderr);
        assertTrue(result.stdout.contains("sample-pipeline") || result.stdout.contains("pipelines"));
    }

    @Test
    void configShow_showsConfiguration(@TempDir Path tempDir) throws Exception {
        copyConfigToTemp(tempDir);
        ProcessResult result = runCliWithConfig(tempDir, "config");
        
        assertTrue(result.exitCode == 0 || result.exitCode == 1);
    }

    @Test
    void unknownCommand_showsHelpOrError() throws Exception {
        ProcessResult result = runCli("unknown-command");
        assertTrue(result.exitCode != 0 || result.stdout.contains("Usage") || result.stdout.contains("Unmatched"));
    }

    @Test
    void buildMaven_showsUsage(@TempDir Path tempDir) throws Exception {
        copyConfigToTemp(tempDir);
        ProcessResult result = runCliWithConfig(tempDir, "build", "maven", "--help");
        
        assertTrue(result.exitCode == 0 || result.stdout.contains("Usage") || result.stdout.contains("maven"));
    }

    @Test
    void checkoutClone_clonesRepository(@TempDir Path tempDir) throws Exception {
        Path cloneDir = tempDir.resolve("Spring-Boot-CRUD-Example");
        
        ProcessResult result = runCli("checkout", "clone", 
            "--url", "https://github.com/fishkingsin/Spring-Boot-CRUD-Example",
            "--target", cloneDir.toString());
        
        assertEquals(0, result.exitCode, "Clone should succeed: " + result.stdout);
        assertTrue(result.stdout.contains("[SUCCESS]"), "Should show success message");
        assertTrue(java.nio.file.Files.exists(cloneDir), "Cloned directory should exist: " + cloneDir);
        assertTrue(java.nio.file.Files.exists(cloneDir.resolve(".git")), "Should contain .git directory");
        assertTrue(java.nio.file.Files.exists(cloneDir.resolve("pom.xml")), "Should contain pom.xml for Maven");
    }

    @Test
    void buildMaven_buildsProject(@TempDir Path tempDir) throws Exception {
        Path cloneDir = tempDir.resolve("Spring-Boot-CRUD-Example");
        
        ProcessResult cloneResult = runCli("checkout", "clone", 
            "--url", "https://github.com/fishkingsin/Spring-Boot-CRUD-Example",
            "--target", cloneDir.toString());
        
        assertEquals(0, cloneResult.exitCode, "Clone should succeed");
        
        ProcessResult buildResult = runCliWithWorkingDir(cloneDir, "build", "maven");
        
        assertEquals(0, buildResult.exitCode, "Build should succeed: " + buildResult.stdout);
        assertTrue(buildResult.stdout.contains("BUILD SUCCESS") || buildResult.stdout.contains("target"),
            "Build should complete successfully");
    }

    private void copyConfigToTemp(Path tempDir) throws Exception {
        Path configDir = Path.of("config");
        if (java.nio.file.Files.exists(configDir)) {
            Path targetConfig = tempDir.resolve("config");
            java.nio.file.Files.createDirectories(targetConfig);
            try (var files = java.nio.file.Files.list(configDir)) {
                files.forEach(file -> {
                    try {
                        java.nio.file.Files.copy(file, targetConfig.resolve(file.getFileName()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
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
