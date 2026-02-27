# CI Engine Core - CLI User Guide

A command-line tool for enterprise Continuous Delivery pipeline orchestration.

## Installation

```bash
# Build the JAR
mvn package -DskipTests

# Run directly
java -jar target/ci-engine-core-1.0.0-SNAPSHOT.jar <command>

# Or create an alias
alias ci-engine='java -jar /path/to/ci-engine-core-1.0.0-SNAPSHOT.jar'
```

## Commands Overview

```
ci-engine <command> [options]

Commands:
  pipeline   - Manage and run pipelines
  stage      - Run individual stages
  deploy     - Deploy to targets
  promote    - Promote between environments
  checkout   - Git checkout operations
  build      - Build artifacts (Maven/Gradle)
  plugin     - Plugin management
  config     - Configuration management
  version    - Show version
```

---

## Pipeline Command

Manage and execute CD pipelines.

### List Pipelines

```bash
ci-engine pipeline list
```

### Run a Pipeline

```bash
# Basic usage
ci-engine pipeline run <pipeline-name>

# Example
ci-engine pipeline run microservice-cd --env production
```

**Options:**
| Option | Description |
|--------|-------------|
| `-e, --env <env>` | Target environment |
| `-v, --var <key=value>` | Variables (comma-separated) |
| `--dry-run` | Validate without executing |

**Example with variables:**
```bash
ci-engine pipeline run microservice-cd \
  --env production \
  --var GIT_COMMIT=abc123,GIT_BRANCH=main
```

---

## Checkout Command

Git repository checkout and clone operations.

### Clone a Repository

```bash
# Clone single repository
ci-engine checkout clone --url https://github.com/org/repo.git

# Clone to specific directory
ci-engine checkout clone --url https://github.com/org/repo.git --target ./myapp

# Clone specific branch
ci-engine checkout clone --url https://github.com/org/repo.git --branch main

# Shallow clone
ci-engine checkout clone --url https://github.com/org/repo.git --depth 10

# With authentication token
ci-engine checkout clone --url https://github.com/org/repo.git --token $GIT_TOKEN
```

### Clone from Config

```bash
ci-engine checkout clone --config config/checkout.yml
```

---

## Build Command

Build artifacts with Maven or Gradle.

### Maven Build

```bash
# Build with default goals (clean package)
ci-engine build maven

# Custom goals
ci-engine build maven --goals "clean compile"

# Skip tests
ci-engine build maven --goals "package" --skip-tests

# Parallel builds
ci-engine build maven --goals "package" --parallel

# Custom pom path
ci-engine build maven --pom myapp/pom.xml
```

### Build from Config

```bash
ci-engine build maven --config config/build.yml
```

---

## Plugin Command

Manage plugins for extensibility.

### List Available Plugins

```bash
ci-engine plugin list
```

### Plugin Types

**Stage Plugins:**
- `security-scan` - SAST, DAST, FOSS scanning
- `performance-test` - K6/JMeter load testing
- `chaos-engineering` - Litmus chaos experiments

**Gate Plugins:**
- `sonarqube` - Code quality gates
- `security-gate` - Security scan gates

**Notifier Plugins:**
- `slack-notify` - Slack notifications
- `email-notify` - Email notifications

### Enable Plugins

Edit `config/plugins.yml`:
```yaml
plugins:
  security-scan:
    enabled: true
    config:
      scanners:
        - sast
        - dast
```

---

## Deploy Command

Deploy applications to Kubernetes or ECS. (Stub)

---

## Promote Command

Promote releases between environments. (Stub)

---

## Stage Command

Run individual pipeline stages. (Stub)

---

## Configuration

### Config Directory

Default: `./config`

```bash
ci-engine --config /path/to/config pipeline run my-pipeline
```

### Config Files

| File | Purpose |
|------|---------|
| `ci-engine.yml` | Global settings |
| `checkout.yml` | Repository configurations |
| `build.yml` | Build tool configurations |
| `deploy.yml` | Deployment settings |
| `environments.yml` | Environment definitions |
| `promote.yml` | Promotion policies |
| `plugins.yml` | Plugin configurations |
| `pipelines/*.yml` | Pipeline definitions |

---

## Examples

### Complete CD Pipeline

```bash
# 1. Checkout code
ci-engine checkout clone --config config/checkout.yml

# 2. Build
ci-engine build maven --config config/build.yml

# 3. Run pipeline
ci-engine pipeline run microservice-cd --env production
```

### Manual Deployment

```bash
# Deploy to dev
ci-engine deploy kubernetes --namespace dev --image myapp:1.0.0

# Promote to staging
ci-engine promote --from dev --to staging

# Promote to production (requires approval)
ci-engine promote --from staging --to production --approve
```

### Using with Jenkins

```groovy
pipeline {
    stages {
        stage('Checkout') {
            steps {
                sh 'ci-engine checkout clone --config config/checkout        }
        
        stage('Build').yml'
            }
 {
            steps {
                sh 'ci-engine build maven --config config/build.yml'
            }
        }
        
        stage('CD Pipeline') {
            steps {
                sh 'ci-engine pipeline run microservice-cd --env production'
            }
        }
    }
}
```

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `CI_ENGINE_CONFIG` | Config directory path |
| `GIT_TOKEN` | Git authentication token |
| `DOCKER_USERNAME` | Docker registry username |
| `DOCKER_PASSWORD` | Docker registry password |
| `AWS_ACCESS_KEY_ID` | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key |
| `SONAR_TOKEN` | SonarQube token |
| `SLACK_WEBHOOK` | Slack webhook URL |

---

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 1 | General error |
| 2 | Configuration validation error |
| 3 | Stage execution failed |
| 4 | Gate evaluation failed |
| 5 | Deployment failed |
| 6 | Promotion rejected |

---

## Build Stages

The CLI supports these built-in stages:

- **build** - Compile code (Maven/Gradle)
- **test** - Run unit/integration tests
- **containerize** - Build Docker images
- **deploy** - Deploy to Kubernetes/ECS

Plugin stages (when enabled):
- security-scan (SAST, DAST, FOSS)
- sonarqube (code quality)
- performance-test (K6, JMeter)
- chaos-engineering (Litmus)

---

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PluginResultTest

# Run with coverage
mvn test -Djacoco
```

---

## Support

- Documentation: https://docs.company.com/ci-engine
- Issues: https://github.com/company/ci-engine/issues
