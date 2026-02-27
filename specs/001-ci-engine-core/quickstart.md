# Quickstart: CI Engine Core CLI

**Date**: 2026-02-28

## Installation

```bash
# Build the JAR
mvn package -DskipTests

# Run directly
java -jar target/ci-engine-core-1.0.0-SNAPSHOT.jar <command>

# Or create an alias
alias ci-engine='java -jar target/ci-engine-core-1.0.0-SNAPSHOT.jar'
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
    config:
      type: kubernetes
      namespace: dev
```

2. Run the pipeline:

```bash
ci-engine pipeline run my-pipeline --env dev
```

3. View available pipelines:

```bash
ci-engine pipeline list
```

## Parallel Execution

Run independent stages in parallel using `dependsOn`:

```yaml
stages:
  - name: unit-tests
    type: test
    config:
      test-type: unit

  - name: security-scan
    type: plugin:security-scan
    config:
      scanners: [sast, oss]

  - name: code-quality
    type: plugin:sonarqube
    config:
      quality-gate: CD_PIPELINE

  - name: containerize
    type: containerize
    dependsOn: [unit-tests, security-scan, code-quality]
```

## Build Tools

### Maven

```yaml
- name: build
  type: build
  config:
    build-tool: maven
    goals: clean package
    skipTests: false
    options: -DskipITs
```

### Gradle

```yaml
- name: build
  type: build
  config:
    build-tool: gradle
    tasks: build -x test
```

### npm

```yaml
- name: build
  type: build
  config:
    build-tool: npm
    command: run build
```

### dotnet

```yaml
- name: build
  type: build
  config:
    build-tool: dotnet
    command: build --configuration Release
```

## Environment Configuration

Create `config/environments/dev.yml`:

```yaml
name: dev
description: Development Environment
order: 1
auto-promote: false
replicas: 2
deploy:
  type: kubernetes
  namespace: dev
  cluster: dev-cluster
  strategy: RollingUpdate
resources:
  cpu: "2"
  memory: "4Gi"
gates:
  - test-passed
```

Create `config/environments/staging.yml`:

```yaml
name: staging
description: Staging Environment
order: 2
auto-promote: false
deploy:
  type: kubernetes
  namespace: staging
  cluster: staging-cluster
  strategy: RollingUpdate
resources:
  cpu: "4"
  memory: "8Gi"
approval:
  type: manual
  roles: [lead, manager]
  timeout: 60m
gates:
  - test-passed
  - coverage-threshold
```

Create `config/environments/prod.yml`:

```yaml
name: prod
description: Production Environment
order: 3
auto-promote: false
deploy:
  type: kubernetes
  namespace: prod
  cluster: prod-cluster
  strategy: BlueGreen
resources:
  cpu: "8"
  memory: "16Gi"
approval:
  type: manual
  roles: [manager, release-manager]
  timeout: 120m
gates:
  - test-passed
  - coverage-threshold
  - security-scan
```

### List Environments

```bash
ci-engine config env list
ci-engine config env show dev
```

## Quality Gates

### Test Passed Gate

```yaml
stages:
  - name: test
    type: test
    
  - name: gate-approval
    type: gate
    config:
      gates:
        - test-passed
```

### Coverage Threshold Gate

```yaml
- name: gate-coverage
  type: gate
  config:
    gates:
      - coverage-threshold
    config:
      coverage-threshold:
        minCoverage: 80
```

### Security Scan Gate

```yaml
- name: gate-security
  type: gate
  config:
    gates:
      - security-scan
    config:
      security-scan:
        maxCritical: 0
        maxHigh: 5
```

### SonarQube Gate

```yaml
- name: gate-sonarqube
  type: gate
  config:
    gates:
      - sonarqube
    config:
      sonarqube:
        minCoverage: 80
        maxCritical: 0
        maxMajor: 10
```

## Deployment Strategies

### Rolling Update

```yaml
- name: deploy
  type: deploy
  config:
    type: kubernetes
    namespace: prod
    strategy: RollingUpdate
    maxSurge: 1
    maxUnavailable: 0
```

### Blue-Green

```yaml
- name: deploy-blue-green
  type: deploy
  config:
    type: kubernetes
    namespace: prod
    strategy: BlueGreen
    deployment: myapp-green
    service: myapp-service
```

### Canary

```yaml
- name: deploy-canary
  type: deploy
  config:
    type: kubernetes
    namespace: prod
    strategy: Canary
    trafficPercentage: 10
```

## Notifications

### Slack Notification

```yaml
notifiers:
  - type: slack
    config:
      webhookUrl: https://hooks.slack.com/services/xxx
      channel: "#deployments"
```

### Email Notification

```yaml
notifiers:
  - type: email
    config:
      smtpHost: smtp.company.com
      smtpPort: 587
      from: ci-engine@company.com
      to: team@company.com
```

## Promotion Policies

### Fast-Track Promotion

```bash
ci-engine promote --from staging --to prod --policy fast-track
```

### Standard Promotion

```bash
ci-engine promote --from staging --to prod --policy standard
```

### Production Promotion (with all gates)

```bash
ci-engine promote --from staging --to prod --policy production
```

### Security Patch Promotion

```bash
ci-engine promote --from staging --to prod --policy security-patch
```

### Show Promotion Chain

```bash
ci-engine config env promote-chain --from dev
```

## Credentials

Set credentials via environment variables:

```bash
export CI_GIT_TOKEN=ghp_xxx
export CI_DOCKER_USERNAME=user
export CI_DOCKER_PASSWORD=pass
export CI_AWS_ACCESS_KEY=AKIAxxx
export CI_AWS_SECRET_KEY=xxx
```

## Commands Overview

| Command | Description |
|---------|-------------|
| `pipeline run <name>` | Execute a pipeline |
| `pipeline list` | List all pipelines |
| `pipeline validate <name>` | Validate pipeline YAML |
| `pipeline status <name>` | Show pipeline execution status |
| `stage run <type>` | Run a single stage |
| `deploy --type kubernetes --namespace ns --image img` | Deploy to Kubernetes |
| `deploy --type ecs --cluster name` | Deploy to ECS |
| `promote --from env1 --to env2 --policy standard` | Promote between environments |
| `config env list` | List environments |
| `config env show <name>` | Show environment details |
| `config env validate <name>` | Validate environment |
| `checkout clone --url <url> --target <dir>` | Clone git repository |
| `build maven` | Build with Maven |
| `build gradle` | Build with Gradle |
| `build npm` | Build with npm |
| `build dotnet` | Build with .NET |
| `plugin list` | List available plugins |
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
| `config/environments/*.yml` | Individual environment configs |
| `config/plugins.yml` | Plugin configurations |
| `config/promote.yml` | Promotion policies |

## Help

```bash
ci-engine --help
ci-engine pipeline --help
ci-engine pipeline run --help
ci-engine config --help
ci-engine promote --help
```
