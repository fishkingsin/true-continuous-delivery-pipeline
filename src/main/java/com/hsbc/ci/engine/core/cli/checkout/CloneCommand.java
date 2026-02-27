package com.hsbc.ci.engine.core.cli.checkout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.util.Map;

@CommandLine.Command(name = "clone", description = "Clone a git repository")
@Component
public class CloneCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(CloneCommand.class);

    @CommandLine.Option(names = {"-u", "--url"}, description = "Repository URL")
    private String url;

    @CommandLine.Option(names = {"-t", "--target"}, description = "Target directory")
    private String target;

    @CommandLine.Option(names = {"-b", "--branch"}, description = "Branch or tag to checkout")
    private String branch;

    @CommandLine.Option(names = {"-d", "--depth"}, description = "Shallow clone depth")
    private Integer depth;

    @CommandLine.Option(names = {"--token"}, description = "Git token for private repos")
    private String token;

    @CommandLine.Option(names = {"-c", "--config"}, description = "Config YAML file")
    private String configFile;

    @Override
    public void run() {
        try {
            if (configFile != null) {
                cloneFromConfig(configFile);
            } else if (url != null) {
                cloneRepository(url, target, branch, depth, token);
            } else {
                System.err.println("[ERROR] Either --url or --config must be provided");
                System.exit(1);
            }
        } catch (Exception e) {
            log.error("Clone failed: {}", e.getMessage());
            System.err.println("[ERROR] Clone failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void cloneFromConfig(String configFile) throws Exception {
        log.info("Loading checkout config from: {}", configFile);
        System.out.println("[INFO] Loading checkout config from: " + configFile);
        
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        java.nio.file.Path path = java.nio.file.Paths.get(configFile);
        if (!java.nio.file.Files.exists(path)) {
            throw new java.io.FileNotFoundException("Config file not found: " + configFile);
        }
        
        Map<String, Object> config = yaml.load(java.nio.file.Files.readString(path));
        Map<String, Object> checkout = (Map<String, Object>) config.get("checkout");
        
        if (checkout == null) {
            throw new IllegalArgumentException("No 'checkout' section found in config");
        }

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> repositories = 
            (java.util.List<Map<String, Object>>) checkout.get("repositories");

        if (repositories == null || repositories.isEmpty()) {
            throw new IllegalArgumentException("No repositories defined in checkout config");
        }

        Map<String, Object> defaults = (Map<String, Object>) checkout.get("defaults");

        for (Map<String, Object> repo : repositories) {
            String repoUrl = (String) repo.get("url");
            String repoTarget = (String) repo.getOrDefault("target", getRepoName(repoUrl));
            String repoBranch = (String) repo.getOrDefault("branch", "main");
            Integer repoDepth = (Integer) repo.getOrDefault("depth", defaults != null ? (Integer) defaults.get("depth") : null);
            String repoToken = (String) repo.get("token");

            log.info("Cloning {} to {}", repoUrl, repoTarget);
            System.out.println("[INFO] Cloning " + repoUrl + " to " + repoTarget);
            cloneRepository(repoUrl, repoTarget, repoBranch, repoDepth, repoToken);
        }
    }

    private void cloneRepository(String url, String targetDir, String branch, Integer depth, String token) throws Exception {
        if (targetDir == null) {
            targetDir = getRepoName(url);
        }

        String effectiveUrl = url;
        if (token != null && url.startsWith("https://")) {
            effectiveUrl = url.replace("https://", "https://oauth2:" + token + "@");
        }

        java.util.List<String> cmd = new java.util.ArrayList<>();
        cmd.add("git");
        cmd.add("clone");

        if (depth != null && depth > 0) {
            cmd.add("--depth");
            cmd.add(String.valueOf(depth));
        }

        if (branch != null) {
            cmd.add("--branch");
            cmd.add(branch);
            cmd.add("--single-branch");
        }

        cmd.add(effectiveUrl);
        cmd.add(targetDir);

        log.debug("Running: {}", String.join(" ", cmd));
        System.out.println("[INFO] Running: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Git clone failed with exit code: " + exitCode);
        }

        log.info("Cloned to: {}", new File(targetDir).getAbsolutePath());
        System.out.println("[SUCCESS] Cloned to: " + new File(targetDir).getAbsolutePath());
    }

    private String getRepoName(String url) {
        String name = url.replaceAll(".*/", "").replaceAll("\\.git$", "");
        return name;
    }
}
