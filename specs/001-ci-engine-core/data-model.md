# Data Model: CI Engine Core CLI

**Date**: 2026-02-28  
**Feature**: CI Engine Core CLI

## Entities

### Pipeline

Represents a complete CD pipeline definition with stages and configuration.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Unique identifier for the pipeline |
| version | String | No | Pipeline version |
| description | String | No | Human-readable description |
| stages | List<Stage> | Yes | Ordered list of stages to execute |
| environments | List<String> | No | Environment promotion chain |

**Validation Rules:**
- `name` must be non-empty, alphanumeric with hyphens/underscores
- `stages` must contain at least one stage

---

### Stage

Represents a single step in a pipeline.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Unique name within the pipeline |
| type | String | Yes | Stage type: build, test, deploy, containerize, checkout, plugin:security-scan, plugin:sonarqube, etc. |
| config | Map<String, Object> | No | Stage-specific configuration |
| dependsOn | List<String> | No | Stages that must complete first (enables parallel execution) |
| enabled | Boolean | No | Whether to execute (default: true) |
| retry | Integer | No | Number of retry attempts on failure |
| timeout | Integer | No | Stage-specific timeout in minutes |
| environment | String | No | Target environment |
| target | String | No | Deployment target (kubernetes, ecs) |
| gates | List<Gate> | No | Quality gates that must pass |
| autoPromote | Boolean | No | Auto-promote after stage completes |

**Validation Rules:**
- `name` must be non-empty
- `type` must be a registered stage type
- `dependsOn` references must exist in the pipeline

---

### Environment

Represents a target deployment environment.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Environment identifier (dev, staging, prod, uat, sandbox) |
| description | String | No | Human-readable description |
| order | Integer | No | Promotion order |
| autoPromote | Boolean | No | Auto-promote to next environment |
| deploy.type | String | No | Deployment type (kubernetes, ecs) |
| deploy.namespace | String | No | Kubernetes namespace |
| deploy.cluster | String | No | ECS cluster |
| deploy.strategy | String | No | Deployment strategy (rolling, blue-green, canary) |
| approval.type | String | No | Approval type (manual) |
| approval.roles | List<String> | No | Roles required for approval |
| gates | List<String> | No | Quality gates for this environment |
| resources.cpu | String | No | CPU allocation (e.g., 1000m) |
| resources.memory | String | No | Memory allocation (e.g., 1Gi) |
| replicas | Integer | No | Number of replicas |
| monitoring | Map<String, Boolean> | No | Monitoring options |

---

### Gate

Quality gate that must pass before proceeding.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| type | String | Yes | Gate type: test-passed, coverage, security-scan, code-quality, deployment-successful, performance-baseline |
| min | Integer | No | Minimum coverage percentage |
| maxCritical | Integer | No | Maximum critical security findings |
| maxHigh | Integer | No | Maximum high security findings |
| qualityGate | String | No | SonarQube quality gate status |
| rating | String | No | SonarQube rating (A-F) |

---

### BuildConfig

Build tool configuration.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| defaultTool | String | No | Default build tool (maven, gradle, npm, dotnet) |
| tools | Map<String, Object> | No | Tool-specific settings |
| projects | List<BuildProject> | No | Multi-project build configuration |

---

### CheckoutConfig

Git repository checkout configuration.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| repositories | List<Repository> | No | Repositories to checkout |
| defaultDepth | Integer | No | Default clone depth |
| defaultTimeout | Integer | No | Default timeout in seconds |

---

### Credential

Credential configuration for external services.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| type | String | Yes | Credential type (git, docker, aws) |
| token | String | No | Authentication token |
| username | String | No | Username |
| password | String | No | Password |
| region | String | No | AWS region |
| accessKeyId | String | No | AWS access key |
| secretAccessKey | String | No | AWS secret key |

---

### Notification

Notification configuration.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| type | String | Yes | Notification type (slack, email) |
| enabled | Boolean | No | Whether notifications are enabled |
| webhookUrl | String | No | Slack webhook URL |
| channel | String | No | Slack channel |
| smtpHost | String | No | SMTP server host |
| from | String | No | Email sender address |
| to | List<String> | No | Email recipient addresses |

---

### PromotionPolicy

Promotion policy configuration.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Policy name (fast-track, standard, production, security-patch) |
| description | String | No | Policy description |
| conditions | List<Condition> | No | Conditions to trigger this policy |
| requirements | List<Requirement> | No | Requirements that must be met |

---

### PipelineContext

Runtime execution context passed through pipeline stages.

| Field | Type | Description |
|-------|------|-------------|
| pipelineName | String | Name of pipeline being executed |
| environment | String | Target environment |
| dryRun | Boolean | If true, stages execute without side effects |
| variables | Map<String, String> | Runtime variables |
| stageResults | Map<String, StageResult> | Results from completed stages |

---

### StageResult

Result of a stage execution.

| Field | Type | Description |
|-------|------|-------------|
| stageName | String | Name of the executed stage |
| success | Boolean | Whether the stage completed successfully |
| output | String | Stage execution output/logs |
| durationMs | long | Execution time in milliseconds |
| metadata | Map<String, String> | Additional stage-specific data |

---

### PipelineResult

Overall result of pipeline execution.

| Field | Type | Description |
|-------|------|-------------|
| success | Boolean | Whether all stages completed successfully |
| error | String | Error message if pipeline failed |
| context | PipelineContext | Execution context with all results |

---

## Relationships

```
Pipeline
  ├── contains → Stage (1..*)
  ├── has environments → (promotion chain)
  └── produces → PipelineResult
      └── contains → StageResult (1..*)

Stage
  ├── has → Gate (0..*)
  └── references → Environment (0..1)

Environment
  └── has → Gate (0..*)
```

## State Transitions

### Pipeline Execution States

```
PENDING → RUNNING → COMPLETED (success)
                → FAILED (stage failure)
                → CANCELLED (user cancellation)
```

### Stage Execution States

```
PENDING → RUNNING → COMPLETED
                 → FAILED
                 → SKIPPED (dependency failed)
                 → RETRYING (retry in progress)
```
