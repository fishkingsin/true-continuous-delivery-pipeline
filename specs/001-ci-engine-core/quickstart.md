# Quickstart: CD Engine CLI

**Date**: 2026-02-27

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
environment: dev

stages:
  - name: build
    type: build
    config:
      command: mvn package -DskipTests

  - name: test
    type: test
    config:
      command: mvn test

  - name: deploy
    type: deploy
    config:
      target: local
```

2. Run the pipeline:

```bash
ci-engine-core pipeline run my-pipeline --env dev
```

3. View available pipelines:

```bash
ci-engine-core pipeline list
```

## Environment Configuration

Create environment configs in `config/environments/`:

```yaml
# config/environments/dev.yml
name: dev
url: http://localhost:8080
variables:
  REGION: us-east-1
  REPLICA_COUNT: "1"
```

## Commands Overview

| Command                    | Description                  |
| -------------------------- | ---------------------------- |
| `pipeline run <name>`      | Execute a pipeline           |
| `pipeline list`            | List all pipelines           |
| `pipeline validate <file>` | Validate pipeline YAML       |
| `stage run <type>`         | Run a single stage           |
| `deploy <target>`          | Deploy to target             |
| `promote <from> <to>`      | Promote between environments |
| `config show`              | Show current configuration   |
| `version`                  | Show version info            |

## Help

```bash
ci-engine-core --help
ci-engine-core pipeline --help
ci-engine-core pipeline run --help
```
