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

### Validate Pipeline

```bash
ci-engine pipeline validate <file>
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

**Config file format (`config/checkout.yml`):**
```yaml
checkout:
  repositories:
    - name: myapp
      url: https://github.com/org/myapp.git
      branch: main
      target: ./repos/myapp
      depth: 10
      
  defaults:
    depth: 5
```

---

## Deploy Command

Deploy applications to Kubernetes or ECS.

### Deploy to Kubernetes

```bash
ci-engine deploy kubernetes \
  --namespace dev \
  --image myapp:1.0.0 \
  --replicas 3
```

### Deploy to ECS

```bash
ci-engine deploy ecs \
  --cluster prod-cluster \
  --service myapp \
  --image myapp:1.0.0
```

---

## Promote Command

Promote releases between environments.

```bash
# Promote from one environment to another
ci-engine promote --from uat --to production

# With specific policy
ci-engine promote --from uat --to production --policy standard

# Approve manual promotion
ci-engine promote --from uat --to production --approve
```

---

## Stage Command

Run individual pipeline stages.

```bash
# Run a single stage
ci-engine stage run build

# With config
ci-engine stage run build --config stages/build.yml
```

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
| `deploy.yml` | Deployment settings |
| `environments.yml` | Environment definitions |
| `promote.yml` | Promotion policies |
| `pipelines/*.yml` | Pipeline definitions |

---

## Examples

### Complete CD Pipeline

```bash
# 1. Checkout code
ci-engine checkout clone --config config/checkout.yml

# 2. Run pipeline
ci-engine pipeline run microservice-cd --env production
```

### Manual Deployment

```bash
# Deploy to dev
ci-engine deploy kubernetes \
  --namespace dev \
  --image myapp:1.0.0

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
                sh 'ci-engine checkout clone --config config/checkout.yml'
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

Plugin stages (future):
- security-scan (SAST, DAST, FOSS)
- sonarqube (code quality)
- performance-test (K6, JMeter)
- chaos-engineering (Litmus)

---

## Support

- Documentation: https://docs.company.com/ci-engine
- Issues: https://github.com/company/ci-engine/issues
