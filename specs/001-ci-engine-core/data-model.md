# Data Model: CD Engine CLI

**Date**: 2026-02-27  
**Feature**: CD Engine CLI

## Entities

### Pipeline

Represents a complete CD pipeline definition with stages and configuration.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Unique identifier for the pipeline |
| description | String | No | Human-readable description |
| stages | List<Stage> | Yes | Ordered list of stages to execute |
| environment | String | No | Default target environment |
| variables | Map<String, String> | No | Default variables for all stages |
| timeout | Integer | No | Maximum execution time in minutes |

**Validation Rules:**
- `name` must be non-empty, alphanumeric with hyphens/underscores
- `stages` must contain at least one stage
- `timeout` must be positive if specified

---

### Stage

Represents a single step in a pipeline.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Unique name within the pipeline |
| type | String | Yes | Stage type: build, test, deploy, containerize |
| config | Map<String, Object> | No | Stage-specific configuration |
| dependsOn | List<String> | No | Stages that must complete first |
| enabled | Boolean | No | Whether to execute (default: true) |
| retry | Integer | No | Number of retry attempts on failure |
| timeout | Integer | No | Stage-specific timeout in minutes |

**Validation Rules:**
- `name` must be non-empty
- `type` must be a registered stage type
- `dependsOn` references must exist in the pipeline

---

### Environment

Represents a target deployment environment.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Environment identifier (dev, staging, prod) |
| url | String | Yes | Target deployment URL |
| credentials | String | No | Reference to stored credentials |
| variables | Map<String, String> | No | Environment-specific variables |
| namespace | String | No | Kubernetes namespace (if applicable) |

**Validation Rules:**
- `name` must be non-empty
- `url` must be a valid URL if specified

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
  ├── references → Environment (0..1)
  └── produces → PipelineResult
      └── contains → StageResult (1..*)
```

---

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
