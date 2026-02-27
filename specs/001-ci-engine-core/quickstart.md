# Quickstart: CI Engine Core CLI

**Date**: 2026-02-28

## Installation

```bash
# Build the JAR
mvn package -DskipTests

# Run directly
java -jar target/ci-engine-core-1.0.0-SNAPSHOT.jar <command>

# Or create an alias
alias ci-engine-core='java -jar target/ci-engine-core-1.0.0-SNAPSHOT.jar'
```

## First Pipeline

1. Create a pipeline definition in `config/pipelines/my-pipeline.yml`:

```yaml
name: my-pipeline
description: My first CD pipeline
version: "1.0"

stages:
  - name: build
    type: build
    config:
      build-tool: maven
      goals: clean package -DskipTests

  - name: test
    type: test
    config:
      test-type: unit

  - name: deploy
    type: deploy
    target: kubernetes
    environment: dev
```

2. Run the pipeline:

```bash
ci-engine-core pipeline run my-pipeline --env dev
```

3. View available pipelines:

```bash
ci-engine-core pipeline list
```

## Parallel Execution

Run independent stages in parallel using `dependsOn`:

```yaml
stages:
  - name: unit-tests
    type: test
    test-type: unit
  
  - name: security-scan
    type: plugin:security-scan
    config:
      scanners: [sast, foss]
  
  - name: code-quality
    type: plugin:sonarqube
    config:
      quality-gate: CD_PIPELINE
  
  # These run after the parallel stages complete
  - name: containerize
    type: containerize
    dependsOn: [unit-tests, security-scan, code-quality]
```

## Commands Overview

| Command | Description |
|---------|-------------|
| `pipeline run <name>` | Execute a pipeline |
| `pipeline list` | List all pipelines |
| `pipeline validate <file>` | Validate pipeline YAML |
| `stage run <type>` | Run a single stage |
| `deploy <target>` | Deploy to target |
| `promote <from> <to>` | Promote between environments |
| `config show` | Show current configuration |
| `version` | Show version info |

## Configuration Files

| File | Purpose |
|------|---------|
| `config/pipelines/*.yml` | Pipeline definitions |
| `config/build.yml` | Build tools (Maven, Gradle, npm, dotnet) |
| `config/checkout.yml` | Git repository checkout |
| `config/ci-engine.yml` | Global settings, credentials |
| `config/deploy.yml` | Kubernetes/ECS deployment configs |
| `config/environments.yml` | Environment definitions |
| `config/plugins.yml` | Plugin configurations |
| `config/promote.yml` | Promotion policies |

## Help

```bash
ci-engine-core --help
ci-engine-core pipeline --help
ci-engine-core pipeline run --help
```
