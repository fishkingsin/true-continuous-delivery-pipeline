# CD Engine - Architecture Design

**Date:** 2026-02-27  
**Version:** 1.0.0

## 1. Overview

The CD Engine is a Spring Boot + Picocli CLI application that empowers existing CI tools (Jenkins, Ansible) to enable enterprise-grade Continuous Delivery pipelines. It provides a high-maintainable, extensible, and configurable pipeline engine without requiring DevOps to develop complex, project-specific pipeline scripts.

## 2. Core Principle

**Jenkins calls the CLI, not the other way around.** This keeps Jenkinsfiles simple while the CD Engine provides enterprise-grade pipeline capabilities.

```groovy
// Jenkinsfile - Simple and maintainable
pipeline {
    stages {
        stage('CD Pipeline') {
            steps {
                sh 'cd-engine pipeline run microservice-cd --env production'
            }
        }
    }
}
```

## 3. Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Jenkins CI                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Jenkinsfile - Single line call to cd-engine            │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CD Engine CLI (Picocli)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │ CLI Commands │  │ Pipeline    │  │ Extension Manager      │ │
│  │ - pipeline   │  │ Orchestrator│  │ (Plugin Placeholders)  │ │
│  │ - stage      │  │             │  │                        │ │
│  │ - deploy     │  │             │  │                        │ │
│  │ - promote    │  │             │  │                        │ │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
       ┌──────────┐    ┌──────────┐    ┌──────────┐
       │ Pipeline │    │  Config  │    │ Built-in │
       │ YAML     │    │ Files    │    │ Stages   │
       └──────────┘    └──────────┘    └──────────┘
```

## 4. Pipeline Stages (Built-in)

| Stage | Description |
|-------|-------------|
| `build` | Compile and package application |
| `test` | Run unit, integration tests |
| `security-scan` | SAST, DAST, FOSS scanning (placeholder) |
| `code-quality` | SonarQube integration (placeholder) |
| `containerize` | Docker build and push |
| `deploy` | Deploy to Kubernetes or ECS |

### Extended Stages (Plugin Placeholders)

| Stage | Description |
|-------|-------------|
| `performance-test` | K6/Locust load testing |
| `chaos-engineering` | Litmus chaos experiments |
| `inter-service-test` | Multi-service integration tests |

## 5. Environment Promotion Chain

```
sandbox → integration → dev → UAT → performance → production
```

Each environment supports:
- **Auto-promote:** Automatic promotion after gates pass
- **Manual approval:** Require explicit approval
- **Gates:** Quality gates that must pass before promotion

## 6. Configuration

### Pipeline YAML (`pipelines/<name>.yml`)

```yaml
name: microservice-cd
stages:
  - name: build
    type: build
  - name: test
    type: test
  - name: containerize
    type: containerize
  - name: deploy-dev
    type: deploy
    environment: dev
```

### Environments YAML (`config/environments.yml`)

```yaml
environments:
  dev:
    auto-promote: true
    deploy:
      type: kubernetes
      namespace: dev
  production:
    auto-promote: false
    approval:
      roles: [release-manager]
```

## 7. CLI Commands

| Command | Description |
|---------|-------------|
| `pipeline run <name>` | Execute a pipeline |
| `pipeline list` | List available pipelines |
| `stage run <name>` | Run a single stage |
| `deploy <target>` | Deploy to target |
| `promote --from X --to Y` | Promote between environments |

## 8. Extension Points (Future)

Plugin system with interfaces for:
- `StagePlugin` - Custom pipeline stages
- `DeployerPlugin` - Target-specific deployment
- `GatingPolicy` - Custom promotion policies

## 9. Build & Distribution

- **Build Tool:** Maven
- **Packaging:** Executable JAR with Picocli
- **Distribution:** Shared via internal Maven repo or direct JAR

## 10. Integration with External Tools

| Tool | Integration |
|------|-------------|
| Jenkins | CLI call from Jenkinsfile |
| Ansible | Called for IaC provisioning |
| Docker | Docker build/push commands |
| kubectl | Kubernetes deployment |
| AWS CLI | ECS deployment |

## 11. File Structure

```
cd-engine/
├── pom.xml
├── src/main/java/com/cdengine/
│   ├── cli/               # Picocli commands
│   ├── orchestrator/     # Pipeline execution
│   ├── config/           # Configuration loading
│   ├── stages/           # Built-in stages
│   ├── plugins/          # Plugin interfaces (placeholder)
│   └── model/            # Domain models
├── src/main/resources/
│   ├── application.yml
│   └── default-pipeline.yml
└── config/               # External configuration
    ├── environments.yml
    └── pipelines/
```

## 12. CLI Command Design

### 12.1 Command Hierarchy

```
cd-engine (root command)
├── pipeline
│   ├── run <pipeline-name>
│   ├── list
│   └── validate <file>
├── stage
│   ├── run <stage-name>
│   └── list
├── deploy
│   ├── kubernetes
│   └── ecs
├── promote
│   └── --from <env> --to <env>
├── plugin
│   ├── list
│   └── install <jar-file>
├── config
│   └── validate
└── version
```

### 12.2 Root Command

```java
@Command(name = "cd-engine",
         description = "Enterprise CD Pipeline Engine",
         footer = "Documentation: https://docs.company.com/cd-engine",
         subcommands = {
             PipelineCommand.class,
             StageCommand.class,
             DeployCommand.class,
             PromoteCommand.class,
             PluginCommand.class,
             ConfigCommand.class,
             VersionCommand.class
         })
@SpringBootApplication
public class CdEngineCommand implements Runnable, ExitCodeGenerator {
    
    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;
    
    @Option(names = {"-c", "--config"}, description = "Config directory path")
    private String configPath = "config";
    
    public static void main(String[] args) {
        int exitCode = SpringApplication.exit(
            SpringApplication.run(CdEngineCommand.class, args),
            () -> 0
        );
        System.exit(exitCode);
    }
    
    @Override
    public void run() {
        // Show help if no subcommand provided
    }
}
```

### 12.3 Pipeline Command

```java
@Command(name = "pipeline",
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

@Command(name = "run",
         description = "Execute a pipeline")
public class PipelineRunCommand implements Runnable {
    
    @Parameters(index = "0", description = "Pipeline name")
    private String pipelineName;
    
    @Option(names = {"-e", "--env"}, description = "Target environment")
    private String environment;
    
    @Option(names = {"-v", "--var"}, description = "Variables (key=value)")
    private Map<String, String> variables = new HashMap<>();
    
    @Option(names = {"--dry-run"}, description = "Validate without executing")
    private boolean dryRun;
    
    @Autowired private PipelineOrchestrator orchestrator;
    
    @Override
    public void run() {
        PipelineContext context = PipelineContext.builder()
            .pipelineName(pipelineName)
            .environment(environment)
            .variables(variables)
            .build();
        
        PipelineResult result = orchestrator.execute(context);
        
        if (result.isSuccess()) {
            System.out.println("Pipeline completed successfully");
        } else {
            System.err.println("Pipeline failed: " + result.getError());
            System.exit(1);
        }
    }
}
```

### 12.4 Stage Command

```java
@Command(name = "stage", description = "Stage operations")
public class StageCommand implements Runnable {
    @Override
    public void run() {}
}

@Command(name = "run", description = "Execute a single stage")
public class StageRunCommand implements Runnable {
    
    @Parameters(index = "0", description = "Stage name")
    private String stageName;
    
    @Option(names = {"-c", "--config"}, description = "Stage config YAML file")
    private String configFile;
    
    @Autowired private StageExecutor stageExecutor;
    
    @Override
    public void run() {
        StageResult result = stageExecutor.execute(stageName, configFile);
        // Handle result...
    }
}
```

### 12.5 Deploy Command

```java
@Command(name = "deploy", description = "Deployment operations")
public class DeployCommand implements Runnable {
    @Override
    public void run() {}
}

@Command(name = "kubernetes",
         description = "Deploy to Kubernetes")
public class K8sDeployCommand implements Runnable {
    
    @Option(names = {"-n", "--namespace"}, required = true)
    private String namespace;
    
    @Option(names = {"-i", "--image"}, required = true)
    private String image;
    
    @Option(names = {"--deployment"}, description = "Deployment name")
    private String deployment;
    
    @Option(names = {"-r", "--replicas"}, defaultValue = "1")
    private int replicas;
    
    @Autowired private KubernetesDeployer deployer;
    
    @Override
    public void run() {
        DeploymentResult result = deployer.deploy(DeployRequest.builder()
            .namespace(namespace)
            .image(image)
            .replicas(replicas)
            .build());
        
        System.out.println("Deployed to " + namespace);
    }
}
```

### 12.6 Promote Command

```java
@Command(name = "promote",
         description = "Promote between environments")
public class PromoteCommand implements Runnable {
    
    @Option(names = {"--from"}, required = true)
    private String fromEnvironment;
    
    @Option(names = {"--to"}, required = true)
    private String toEnvironment;
    
    @Option(names = {"--policy"}, description = "Promotion policy name")
    private String policyName;
    
    @Option(names = {"--approve"}, description = "Approve manual promotion")
    private boolean approved;
    
    @Autowired private EnvironmentManager envManager;
    
    @Override
    public void run() {
        PromotionResult result = envManager.promote(
            PromotionRequest.builder()
                .from(fromEnvironment)
                .to(toEnvironment)
                .policy(policyName)
                .approved(approved)
                .build());
        
        if (result.requiresApproval()) {
            System.out.println("Manual approval required for " + toEnvironment);
            System.out.println("Run: cd-engine promote --from " + fromEnvironment + 
                             " --to " + toEnvironment + " --approve");
        }
    }
}
```

### 12.7 CLI Output Examples

```bash
# Run pipeline
$ cd-engine pipeline run microservice-cd --env production
[INFO] Loading pipeline: microservice-cd
[INFO] Environment: production
[INFO] 
[INFO] ╔═══════════════════════════════════════════════════════╗
[INFO] ║  CD Pipeline: microservice-cd                       ║
[INFO] ╠═══════════════════════════════════════════════════════╣
[INFO] ║  Stage: build                   [██████████] 100%   ║
[INFO] ║  Stage: test                    [██████████] 100%   ║
[INFO] ║  Stage: containerize            [██████████] 100%   ║
[INFO] ║  Stage: deploy-sandbox          [██████████] 100%   ║
[INFO] ║  Stage: deploy-dev              [██████████] 100%   ║
[INFO] ║  Stage: deploy-uat              [..........]  45%   ║
[INFO] ╠═══════════════════════════════════════════════════════╣
[INFO] ║  [PAUSED] Waiting for approval to production         ║
[INFO] ╚═══════════════════════════════════════════════════════╝

# List pipelines
$ cd-engine pipeline list
Available pipelines:
  microservice-cd        - Standard microservice pipeline
  java-app-cd           - Java application pipeline
  frontend-cd           - Frontend SPA pipeline

# Validate config
$ cd-engine config validate --file pipelines/my-pipeline.yml
[INFO] Validating: pipelines/my-pipeline.yml
[INFO] ✓ Pipeline syntax valid
[INFO] ✓ All referenced stages exist
[INFO] ✓ Environment references valid
[SUCCESS] Configuration is valid
```

### 12.8 Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | General error |
| 2 | Configuration validation error |
| 3 | Stage execution failed |
| 4 | Gate evaluation failed |
| 5 | Deployment failed |
| 6 | Promotion rejected (gates/approval) |
