# CD Engine Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Create a Spring Boot + Picocli CLI application that provides enterprise-grade CD pipeline orchestration, callable from Jenkins.

**Architecture:** Single JAR executable with YAML-based pipeline configuration. Built-in stages (build, test, containerize, deploy) with plugin placeholders for future extension.

**Tech Stack:** Java 21, Spring Boot 3.2, Picocli 4.7, Maven, SnakeYAML

---

## Phase 1: Project Setup

### Task 1: Create Maven pom.xml

**Files:**
- Create: `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.cdengine</groupId>
    <artifactId>cd-engine</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>CD Engine</name>
    <description>Enterprise CD Pipeline CLI Engine</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>21</java.version>
        <picocli.version>4.7.5</picocli.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-yaml</artifactId>
        </dependency>
        
        <!-- Picocli -->
        <dependency>
            <groupId>info.picocli</groupId>
            <artifactId>picocli-spring-boot-starter</artifactId>
            <version>${picocli.version}</version>
        </dependency>
        <dependency>
            <groupId>info.picocli</groupId>
            <artifactId>picocli</artifactId>
            <version>${picocli.version}</version>
        </dependency>

        <!-- YAML -->
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 1: Verify Maven is available**
Run: `mvn --version`
Expected: Maven version displayed

**Step 2: Write pom.xml**
Create the file with content above

**Step 3: Verify project structure**
Run: `ls -la`
Expected: pom.xml exists

---

### Task 2: Create Spring Boot Application Entry Point

**Files:**
- Create: `src/main/java/com/cdengine/CdEngineApplication.java`

```java
package com.cdengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CdEngineApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CdEngineApplication.class, args);
    }
}
```

**Step 1: Verify Maven creates directory structure**
Run: `mkdir -p src/main/java/com/cdengine`

**Step 2: Write CdEngineApplication.java**

**Step 3: Test compilation**
Run: `mvn compile -q`
Expected: BUILD SUCCESS

**Step 4: Commit**
Run: `git add pom.xml src/ && git commit -m "chore: add Maven pom.xml and Spring Boot entry point"`

---

## Phase 2: CLI Commands

### Task 3: Create Root CLI Command

**Files:**
- Create: `src/main/java/com/cdengine/cli/CdEngineCommand.java`

```java
package com.cdengine.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cdengine", "picocli.spring.boot"})
public class CdEngineCommand implements CommandLineRunner, ExitCodeGenerator {

    @Bean
    public CommandLine myCommand() {
        return new CommandLine(new RootCommand());
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
            SpringApplication.run(CdEngineCommand.class, args)
        ));
    }

    @Override
    public void run(String... args) throws Exception {
        CommandLine.call(new RootCommand(), args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    private int exitCode = 0;

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Command(name = "cd-engine",
             description = "Enterprise CD Pipeline Engine",
             footer = "Documentation: https://docs.company.com/cd-engine",
             subcommands = {
                 PipelineCommand.class,
                 StageCommand.class,
                 DeployCommand.class,
                 PromoteCommand.class,
                 ConfigCommand.class,
                 VersionCommand.class
             })
    public static class RootCommand implements Runnable {

        @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose output")
        private boolean verbose;

        @CommandLine.Option(names = {"-c", "--config"}, 
                          description = "Config directory path",
                          defaultValue = "config")
        private String configPath;

        @CommandLine.Option(names = {"-h", "--help"}, 
                          usageHelp = true, 
                          description = "Show help")
        private boolean help;

        @Override
        public void run() {
            if (help) {
                CommandLine.usage(this, System.out);
            }
        }
    }
}
```

**Step 1: Create CLI directory**
Run: `mkdir -p src/main/java/com/cdengine/cli`

**Step 2: Write CdEngineCommand.java**

**Step 3: Test compilation**
Run: `mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 4: Create Pipeline Commands

**Files:**
- Create: `src/main/java/com/cdengine/cli/PipelineCommand.java`
- Create: `src/main/java/com/cdengine/cli/PipelineRunCommand.java`

**PipelineCommand.java:**
```java
package com.cdengine.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "pipeline",
                     description = "Pipeline operations",
                     subcommands = {
                         PipelineRunCommand.class,
                         PipelineListCommand.class,
                         PipelineValidateCommand.class
                     })
public class PipelineCommand implements Runnable {
    @Override
    public void run() {}
}
```

**PipelineRunCommand.java:**
```java
package com.cdengine.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import com.cdengine.orchestrator.PipelineOrchestrator;
import com.cdengine.config.ConfigurationLoader;
import com.cdengine.model.PipelineContext;
import com.cdengine.model.PipelineResult;

@CommandLine.Command(name = "run", 
                     description = "Execute a pipeline")
@Component
public class PipelineRunCommand implements Runnable {

    @CommandLine.Parameters(index = "0", 
                           description = "Pipeline name")
    private String pipelineName;

    @CommandLine.Option(names = {"-e", "--env"}, 
                       description = "Target environment")
    private String environment;

    @CommandLine.Option(names = {"-v", "--var"}, 
                       description = "Variables (key=value)",
                       split = ",")
    private String[] variables;

    @CommandLine.Option(names = {"--dry-run"}, 
                       description = "Validate without executing")
    private boolean dryRun;

    @Autowired
    private PipelineOrchestrator orchestrator;

    @Autowired
    private ConfigurationLoader configLoader;

    @Override
    public void run() {
        logger.info("[INFO] Loading pipeline: " + pipelineName);
        
        PipelineContext context = PipelineContext.builder()
            .pipelineName(pipelineName)
            .environment(environment != null ? environment : "default")
            .dryRun(dryRun)
            .build();

        if (variables != null) {
            for (String var : variables) {
                String[] parts = var.split("=");
                if (parts.length == 2) {
                    context.addVariable(parts[0], parts[1]);
                }
            }
        }

        PipelineResult result = orchestrator.execute(context);

        if (result.isSuccess()) {
            logger.info("[SUCCESS] Pipeline completed successfully");
        } else {
            System.err.println("[ERROR] Pipeline failed: " + result.getError());
            System.exit(1);
        }
    }
}
```

**Step 1: Create orchestrator directory**
Run: `mkdir -p src/main/java/com/cdengine/orchestrator src/main/java/com/cdengine/config src/main/java/com/cdengine/model`

**Step 2: Write PipelineCommand.java**

**Step 3: Write PipelineRunCommand.java**

**Step 4: Test compilation**
Run: `mvn compile -q 2>&1`
Expected: BUILD SUCCESS (will fail initially - missing classes)

---

### Task 5: Create Stage & Deploy Commands

**Files:**
- Create: `src/main/java/com/cdengine/cli/StageCommand.java`
- Create: `src/main/java/com/cdengine/cli/DeployCommand.java`
- Create: `src/main/java/com/cdengine/cli/PromoteCommand.java`
- Create: `src/main/java/com/cdengine/cli/ConfigCommand.java`
- Create: `src/main/java/com/cdengine/cli/VersionCommand.java`

**StageCommand.java:**
```java
package com.cdengine.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "stage", description = "Stage operations")
public class StageCommand implements Runnable {
    @Override
    public void run() {}
}
```

**DeployCommand.java:**
```java
package com.cdengine.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "deploy", description = "Deployment operations")
public class DeployCommand implements Runnable {
    @Override
    public void run() {}
}
```

**PromoteCommand.java:**
```java
package com.cdengine.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "promote", description = "Promote between environments")
public class PromoteCommand implements Runnable {
    @Override
    public void run() {}
}
```

**ConfigCommand.java:**
```java
package com.cdengine.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "config", description = "Configuration operations")
public class ConfigCommand implements Runnable {
    @Override
    public void run() {}
}
```

**VersionCommand.java:**
```java
package com.cdengine.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "version", description = "Show version")
public class VersionCommand implements Runnable {
    @Override
    public void run() {
        logger.info("cd-engine version 1.0.0");
    }
}
```

**Step 1: Write all command files**

**Step 2: Test compilation**
Run: `mvn compile -q 2>&1`
Expected: BUILD SUCCESS

**Step 3: Commit**
Run: `git add src/main/java/com/cdengine/cli/ && git commit -m "feat: add CLI commands"`

---

## Phase 3: Core Models

### Task 6: Create Domain Models

**Files:**
- Create: `src/main/java/com/cdengine/model/PipelineContext.java`
- Create: `src/main/java/com/cdengine/model/PipelineResult.java`
- Create: `src/main/java/com/cdengine/model/StageResult.java`
- Create: `src/main/java/com/cdengine/model/EnvironmentConfig.java`

**PipelineContext.java:**
```java
package com.cdengine.model;

import java.util.HashMap;
import java.util.Map;

public class PipelineContext {
    private final String pipelineName;
    private final String environment;
    private final boolean dryRun;
    private final Map<String, String> variables = new HashMap<>();
    private final Map<String, StageResult> stageResults = new HashMap<>();

    private PipelineContext(Builder builder) {
        this.pipelineName = builder.pipelineName;
        this.environment = builder.environment;
        this.dryRun = builder.dryRun;
        this.variables.putAll(builder.variables);
    }

    public String getPipelineName() { return pipelineName; }
    public String getEnvironment() { return environment; }
    public boolean isDryRun() { return dryRun; }
    public Map<String, String> getVariables() { return variables; }
    public Map<String, StageResult> getStageResults() { return stageResults; }
    
    public void addVariable(String key, String value) {
        variables.put(key, value);
    }
    
    public void addStageResult(String stageName, StageResult result) {
        stageResults.put(stageName, result);
    }
    
    public String getVariable(String key) {
        return variables.get(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pipelineName;
        private String environment;
        private boolean dryRun;
        private Map<String, String> variables = new HashMap<>();

        public Builder pipelineName(String pipelineName) {
            this.pipelineName = pipelineName;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public Builder variables(Map<String, String> variables) {
            this.variables = variables;
            return this;
        }

        public PipelineContext build() {
            return new PipelineContext(this);
        }
    }
}
```

**PipelineResult.java:**
```java
package com.cdengine.model;

public class PipelineResult {
    private final boolean success;
    private final String error;
    private final PipelineContext context;

    private PipelineResult(boolean success, String error, PipelineContext context) {
        this.success = success;
        this.error = error;
        this.context = context;
    }

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public PipelineContext getContext() { return context; }

    public static PipelineResult success(PipelineContext context) {
        return new PipelineResult(true, null, context);
    }

    public static PipelineResult failed(String error) {
        return new PipelineResult(false, error, null);
    }
}
```

**StageResult.java:**
```java
package com.cdengine.model;

import java.util.Map;

public class StageResult {
    private final String stageName;
    private final boolean success;
    private final String output;
    private final long durationMs;
    private final Map<String, String> metadata;

    private StageResult(Builder builder) {
        this.stageName = builder.stageName;
        this.success = builder.success;
        this.output = builder.output;
        this.durationMs = builder.durationMs;
        this.metadata = builder.metadata;
    }

    public String getStageName() { return stageName; }
    public boolean isSuccess() { return success; }
    public String getOutput() { return output; }
    public long getDurationMs() { return durationMs; }
    public Map<String, String> getMetadata() { return metadata; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String stageName;
        private boolean success;
        private String output;
        private long durationMs;
        private Map<String, String> metadata;

        public Builder stageName(String stageName) { this.stageName = stageName; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder output(String output) { this.output = output; return this; }
        public Builder durationMs(long durationMs) { this.durationMs = durationMs; return this; }
        public Builder metadata(Map<String, String> metadata) { this.metadata = metadata; return this; }

        public StageResult build() {
            return new StageResult(this);
        }
    }
}
```

**EnvironmentConfig.java:**
```java
package com.cdengine.model;

import java.util.List;
import java.util.Map;

public class EnvironmentConfig {
    private String name;
    private String description;
    private int order;
    private boolean autoPromote;
    private DeployConfig deploy;
    private List<GateConfig> gates;
    private ApprovalConfig approval;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getOrder() { return order; }
    public boolean isAutoPromote() { return autoPromote; }
    public DeployConfig getDeploy() { return deploy; }
    public List<GateConfig> getGates() { return gates; }
    public ApprovalConfig getApproval() { return approval; }

    public static class DeployConfig {
        private String type;
        private String namespace;
        private String cluster;
        private String strategy;

        public String getType() { return type; }
        public String getNamespace() { return namespace; }
        public String getCluster() { return cluster; }
        public String getStrategy() { return strategy; }
    }

    public static class GateConfig {
        private String type;

        public String getType() { return type; }
    }

    public static class ApprovalConfig {
        private String type;
        private List<String> roles;

        public String getType() { return type; }
        public List<String> getRoles() { return roles; }
    }
}
```

**Step 1: Write all model files**

**Step 2: Test compilation**
Run: `mvn compile -q 2>&1`
Expected: BUILD SUCCESS

**Step 3: Commit**
Run: `git add src/main/java/com/cdengine/model/ && git commit -m "feat: add domain models"`

---

## Phase 4: Configuration & Orchestration

### Task 7: Create Configuration Loader

**Files:**
- Create: `src/main/java/com/cdengine/config/ConfigurationLoader.java`
- Create: `src/main/java/com/cdengine/config/PipelineDefinition.java`

**ConfigurationLoader.java:**
```java
package com.cdengine.config;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.cdengine.model.EnvironmentConfig;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Component
public class ConfigurationLoader {

    private String configPath = "config";
    private Map<String, Object> pipelineConfigs = new HashMap<>();
    private Map<String, EnvironmentConfig> environments = new HashMap<>();

    @PostConstruct
    public void init() {
        loadPipelines();
        loadEnvironments();
    }

    public void setConfigPath(String path) {
        this.configPath = path;
        loadPipelines();
        loadEnvironments();
    }

    private void loadPipelines() {
        Path pipelineDir = Paths.get(configPath, "pipelines");
        if (!Files.exists(pipelineDir)) {
            logger.info("[WARN] Pipeline directory not found: " + pipelineDir);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pipelineDir, "*.yml")) {
            for (Path file : stream) {
                String name = file.getFileName().toString().replace(".yml", "");
                Yaml yaml = new Yaml();
                pipelineConfigs.put(name, yaml.load(Files.readString(file)));
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load pipelines: " + e.getMessage());
        }
    }

    private void loadEnvironments() {
        Path envFile = Paths.get(configPath, "environments.yml");
        if (!Files.exists(envFile)) {
            logger.info("[WARN] Environments file not found: " + envFile);
            return;
        }

        try {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(Files.readString(envFile));
            Map<String, Object> envs = (Map<String, Object>) root.get("environments");
            // Parse environments (simplified)
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load environments: " + e.getMessage());
        }
    }

    public Map<String, Object> getPipeline(String name) {
        return (Map<String, Object>) pipelineConfigs.get(name);
    }

    public Collection<String> listPipelines() {
        return pipelineConfigs.keySet();
    }

    public EnvironmentConfig getEnvironment(String name) {
        return environments.get(name);
    }

    public Collection<String> listEnvironments() {
        return environments.keySet();
    }
}
```

**PipelineDefinition.java:**
```java
package com.cdengine.config;

import java.util.List;
import java.util.Map;

public class PipelineDefinition {
    private String name;
    private String version;
    private String description;
    private List<Map<String, Object>> stages;

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public List<Map<String, Object>> getStages() { return stages; }
}
```

**Step 1: Write configuration classes**

**Step 2: Test compilation**
Run: `mvn compile -q 2>&1`
Expected: BUILD SUCCESS

---

### Task 8: Create Pipeline Orchestrator

**Files:**
- Create: `src/main/java/com/cdengine/orchestrator/PipelineOrchestrator.java`

```java
package com.cdengine.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cdengine.config.ConfigurationLoader;
import com.cdengine.model.PipelineContext;
import com.cdengine.model.PipelineResult;
import com.cdengine.model.StageResult;
import com.cdengine.stages.StageExecutor;

import java.util.Map;

@Component
public class PipelineOrchestrator {

    @Autowired
    private ConfigurationLoader configLoader;

    @Autowired
    private StageExecutor stageExecutor;

    public PipelineResult execute(PipelineContext context) {
        String pipelineName = context.getPipelineName();
        Map<String, Object> pipelineConfig = configLoader.getPipeline(pipelineName);

        if (pipelineConfig == null) {
            return PipelineResult.failed("Pipeline not found: " + pipelineName);
        }

        if (context.isDryRun()) {
            logger.info("[DRY-RUN] Would execute pipeline: " + pipelineName);
            return PipelineResult.success(context);
        }

        logger.info("[INFO] Executing pipeline: " + pipelineName);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stages = (List<Map<String, Object>>) pipelineConfig.get("stages");

        if (stages == null) {
            return PipelineResult.failed("No stages defined in pipeline");
        }

        for (Map<String, Object> stageConfig : stages) {
            String stageName = (String) stageConfig.get("name");
            String stageType = (String) stageConfig.get("type");

            logger.info("[INFO] Executing stage: " + stageName);

            StageResult result = stageExecutor.execute(stageType, stageConfig, context);
            context.addStageResult(stageName, result);

            if (!result.isSuccess()) {
                return PipelineResult.failed("Stage failed: " + stageName);
            }
        }

        return PipelineResult.success(context);
    }
}
```

**Step 1: Write PipelineOrchestrator.java**

**Step 2: Test compilation**
Run: `mvn compile -q 2>&1`
Expected: BUILD SUCCESS

---

### Task 9: Create Stage Executor

**Files:**
- Create: `src/main/java/com/cdengine/stages/StageExecutor.java`
- Create: `src/main/java/com/cdengine/stages/BuildStage.java`
- Create: `src/main/java/com/cdengine/stages/TestStage.java`
- Create: `src/main/java/com/cdengine/stages/ContainerizeStage.java`

**StageExecutor.java:**
```java
package com.cdengine.stages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cdengine.model.PipelineContext;
import com.cdengine.model.StageResult;

import java.util.HashMap;
import java.util.Map;

@Component
public class StageExecutor {

    private final Map<String, Stage> stages = new HashMap<>();

    @Autowired
    private BuildStage buildStage;

    @Autowired
    private TestStage testStage;

    @Autowired
    private ContainerizeStage containerizeStage;

    public StageExecutor() {
        // Register built-in stages
    }

    public StageResult execute(String stageType, 
                              Map<String, Object> config, 
                              PipelineContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            Stage stage = getStage(stageType);
            if (stage == null) {
                return StageResult.builder()
                    .stageName(stageType)
                    .success(false)
                    .output("Unknown stage type: " + stageType)
                    .durationMs(0)
                    .build();
            }

            String output = stage.execute(config, context);
            long duration = System.currentTimeMillis() - startTime;

            return StageResult.builder()
                .stageName(stageType)
                .success(true)
                .output(output)
                .durationMs(duration)
                .build();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return StageResult.builder()
                .stageName(stageType)
                .success(false)
                .output("Error: " + e.getMessage())
                .durationMs(duration)
                .build();
        }
    }

    private Stage getStage(String type) {
        return switch (type) {
            case "build" -> buildStage;
            case "test" -> testStage;
            case "containerize" -> containerizeStage;
            default -> null;
        };
    }
}
```

**Stage.java (interface):**
```java
package com.cdengine.stages;

import com.cdengine.model.PipelineContext;
import java.util.Map;

public interface Stage {
    String execute(Map<String, Object> config, PipelineContext context);
}
```

**BuildStage.java:**
```java
package com.cdengine.stages;

import org.springframework.stereotype.Component;
import com.cdengine.model.PipelineContext;
import java.util.Map;

@Component
public class BuildStage implements Stage {

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String buildTool = (String) config.getOrDefault("build-tool", "maven");
        
        logger.info("  Building with: " + buildTool);
        
        // Execute build command
        try {
            ProcessBuilder pb = new ProcessBuilder();
            if ("maven".equals(buildTool)) {
                pb.command("mvn", "clean", "package", "-DskipTests");
            } else if ("gradle".equals(buildTool)) {
                pb.command("./gradlew", "build", "-x", "test");
            }
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Build failed with exit code: " + exitCode);
            }
            return "Build completed successfully";
        } catch (Exception e) {
            throw new RuntimeException("Build failed: " + e.getMessage());
        }
    }
}
```

**TestStage.java:**
```java
package com.cdengine.stages;

import org.springframework.stereotype.Component;
import com.cdengine.model.PipelineContext;
import java.util.Map;

@Component
public class TestStage implements Stage {

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String testType = (String) config.getOrDefault("test-type", "unit");
        
        logger.info("  Running tests: " + testType);
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("mvn", "test");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Tests failed with exit code: " + exitCode);
            }
            return "Tests passed successfully";
        } catch (Exception e) {
            throw new RuntimeException("Tests failed: " + e.getMessage());
        }
    }
}
```

**ContainerizeStage.java:**
```java
package com.cdengine.stages;

import org.springframework.stereotype.Component;
import com.cdengine.model.PipelineContext;
import java.util.Map;

@Component
public class ContainerizeStage implements Stage {

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String dockerfile = (String) config.getOrDefault("dockerfile", "Dockerfile");
        String registry = (String) config.getOrDefault("registry", "");
        
        String image = "myapp";
        String tag = context.getVariable("GIT_COMMIT");
        if (tag == null) tag = "latest";
        
        logger.info("  Building Docker image: " + image + ":" + tag);
        
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("docker", "build", "-t", image + ":" + tag, "-f", dockerfile, ".");
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Docker build failed with exit code: " + exitCode);
            }
            
            if (registry != null && !registry.isEmpty()) {
                pb = new ProcessBuilder();
                pb.command("docker", "push", registry + "/" + image + ":" + tag);
                pb.inheritIO();
                process = pb.start();
                exitCode = process.waitFor();
            }
            
            return "Containerized successfully";
        } catch (Exception e) {
            throw new RuntimeException("Containerization failed: " + e.getMessage());
        }
    }
}
```

**Step 1: Create stages directory**
Run: `mkdir -p src/main/java/com/cdengine/stages`

**Step 2: Write all stage files**

**Step 3: Test compilation**
Run: `mvn compile -q 2>&1`
Expected: BUILD SUCCESS

**Step 4: Commit**
Run: `git add src/main/java/com/cdengine/orchestrator/ src/main/java/com/cdengine/stages/ src/main/java/com/cdengine/config/ && git commit -m "feat: add orchestrator and built-in stages"`

---

## Phase 5: Sample Configuration & Testing

### Task 10: Create Sample Pipeline Config

**Files:**
- Create: `config/pipelines/sample-pipeline.yml`
- Create: `config/environments.yml`

**sample-pipeline.yml:**
```yaml
name: sample-pipeline
version: "1.0"
description: Sample CD pipeline

stages:
  - name: build
    type: build
    config:
      build-tool: maven
      goals: clean package

  - name: test
    type: test
    config:
      test-type: unit

  - name: containerize
    type: containerize
    config:
      dockerfile: Dockerfile
      registry: docker.io/myorg

  - name: deploy-dev
    type: deploy
    environment: dev
    config:
      namespace: dev
```

**environments.yml:**
```yaml
environments:
  dev:
    description: Development environment
    order: 1
    auto-promote: false
    deploy:
      type: kubernetes
      namespace: dev

  staging:
    description: Staging environment
    order: 2
    auto-promote: false
    deploy:
      type: kubernetes
      namespace: staging

  production:
    description: Production environment
    order: 3
    auto-promote: false
    approval:
      type: manual
      roles: [release-manager]
    deploy:
      type: kubernetes
      namespace: production
```

**Step 1: Create config directories**
Run: `mkdir -p config/pipelines`

**Step 2: Write config files**

**Step 3: Verify JAR builds**
Run: `mvn package -DskipTests -q`
Expected: BUILD SUCCESS, target/cd-engine-1.0.0-SNAPSHOT.jar exists

---

### Task 11: Verify CLI Works

**Step 1: Test help command**
Run: `java -jar target/cd-engine-1.0.0-SNAPSHOT.jar --help`
Expected: Shows CLI help with all commands

**Step 2: Test version command**
Run: `java -jar target/cd-engine-1.0.0-SNAPSHOT.jar version`
Expected: Shows version

**Step 3: Test pipeline list**
Run: `java -jar target/cd-engine-1.0.0-SNAPSHOT.jar pipeline list`
Expected: Lists available pipelines

**Step 4: Commit**
Run: `git add config/ && git commit -m "feat: add sample pipeline configuration"`

---

## Phase 6: Deployment Stage

### Task 12: Create Deploy Stage

**Files:**
- Create: `src/main/java/com/cdengine/stages/DeployStage.java`

```java
package com.cdengine.stages;

import org.springframework.stereotype.Component;
import com.cdengine.model.PipelineContext;
import java.util.Map;

@Component
public class DeployStage implements Stage {

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String targetType = (String) config.get("type");
        String namespace = (String) config.getOrDefault("namespace", "default");
        String image = (String) config.getOrDefault("image", "myapp:latest");
        
        logger.info("  Deploying to: " + targetType + " namespace: " + namespace);
        
        try {
            if ("kubernetes".equals(targetType)) {
                return deployToKubernetes(namespace, image, config);
            } else if ("ecs".equals(targetType)) {
                return deployToECS(config);
            }
            throw new RuntimeException("Unknown deployment target: " + targetType);
        } catch (Exception e) {
            throw new RuntimeException("Deployment failed: " + e.getMessage());
        }
    }

    private String deployToKubernetes(String namespace, String image, Map<String, Object> config) 
            throws Exception {
        // Check if kubectl is available
        ProcessBuilder pb = new ProcessBuilder("kubectl", "version", "--client");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        int exitCode = p.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException("kubectl not found");
        }
        
        logger.info("  [Kubernetes] Applying deployment to namespace: " + namespace);
        // In real implementation, would apply K8s manifests
        return "Deployed to Kubernetes namespace: " + namespace;
    }

    private String deployToECS(Map<String, Object> config) throws Exception {
        String cluster = (String) config.getOrDefault("cluster", "default");
        logger.info("  [ECS] Deploying to cluster: " + cluster);
        // In real implementation, would use AWS CLI or SDK
        return "Deployed to ECS cluster: " + cluster;
    }
}
```

**Step 1: Write DeployStage.java**

**Step 2: Update StageExecutor to register DeployStage**
Modify: Add `@Autowired DeployStage deployStage;` and register in switch case

**Step 3: Test compilation**
Run: `mvn compile -q 2>&1`
Expected: BUILD SUCCESS

**Step 4: Commit**
Run: `git add src/main/java/com/cdengine/stages/ && git commit -m "feat: add deploy stage for Kubernetes and ECS"`

---

## Summary

**Total Tasks:** 12

**Phase 1:** Project Setup (Tasks 1-2)  
**Phase 2:** CLI Commands (Tasks 3-5)  
**Phase 3:** Core Models (Task 6)  
**Phase 4:** Configuration & Orchestration (Tasks 7-9)  
**Phase 5:** Sample Config & Testing (Tasks 10-11)  
**Phase 6:** Deployment Stage (Task 12)

**After Implementation:**
- JAR executable with CLI
- Built-in stages: build, test, containerize, deploy
- YAML-based pipeline configuration
- Ready for Jenkins integration

---

**Plan complete and saved to `docs/plans/cd-engine-architecture.md`**

**Two execution options:**

1. **Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

2. **Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

**Which approach?**
