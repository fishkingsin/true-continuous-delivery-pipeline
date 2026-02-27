# CLI Contract: CD Engine

**Date**: 2026-02-27

## Root Command

```bash
ci-engine-core [GLOBAL_OPTIONS] <command> [COMMAND_OPTIONS] [ARGUMENTS]
```

## Global Options

| Option | Description |
|--------|-------------|
| `--help`, `-h` | Show help message |
| `--version`, `-V` | Show version |
| `--verbose`, `-v` | Enable verbose output |
| `--config=<path>` | Configuration directory path |

## Commands

### pipeline

Manage and execute CD pipelines.

#### pipeline run

```bash
ci-engine-core pipeline run <pipeline-name> [OPTIONS]
```

| Argument | Description |
|----------|-------------|
| `pipeline-name` | Name of pipeline to execute |

| Option | Description |
|--------|-------------|
| `--env <name>` | Target environment (required) |
| `--var <key=value>` | Set variable (repeatable) |
| `--dry-run` | Validate without executing |
| `--stage <name>` | Run only up to specified stage |

**Exit Codes:**
- `0`: Pipeline succeeded
- `1`: Pipeline failed
- `2`: Invalid pipeline definition

#### pipeline list

```bash
ci-engine-core pipeline list [OPTIONS]
```

| Option | Description |
|--------|-------------|
| `--format json` | JSON output (default: human-readable) |

#### pipeline validate

```bash
ci-engine-core pipeline validate <file>
```

| Argument | Description |
|----------|-------------|
| `file` | Path to pipeline YAML file |

---

### stage

Run individual pipeline stages.

```bash
ci-engine-core stage run <type> [OPTIONS]
```

| Argument | Description |
|----------|-------------|
| `type` | Stage type (build, test, deploy, containerize) |

| Option | Description |
|--------|-------------|
| `--config <file>` | Stage configuration file |
| `--var <key=value>` | Set variable |

---

### deploy

Deploy artifacts to targets.

```bash
ci-engine-core deploy <target> [OPTIONS]
```

| Argument | Description |
|----------|-------------|
| `target` | Deployment target |

| Option | Description |
|--------|-------------|
| `--env <name>` | Target environment |
| `--artifact <path>` | Artifact to deploy |
| `--timeout <seconds>` | Deployment timeout |

---

### promote

Promote between environments.

```bash
ci-engine-core promote <from> <to> [OPTIONS]
```

| Argument | Description |
|----------|-------------|
| `from` | Source environment |
| `to` | Target environment |

---

### config

Configuration management.

```bash
ci-engine-core config [COMMAND]
```

#### config show

```bash
ci-engine-core config show
```

Show current configuration.

#### config set

```bash
ci-engine-core config set <key> <value>
```

Set configuration value.

---

### version

Show version information.

```bash
ci-engine-core version [--json]
```

| Option | Description |
|--------|-------------|
| `--json` | JSON output format |

## Output Formats

### Human-Readable

```
Pipeline: my-pipeline
Status: COMPLETED
Duration: 2m 34s

Stages:
  ✓ build (1m 12s)
  ✓ test (45s)
  ✓ deploy (37s)
```

### JSON

```json
{
  "pipeline": "my-pipeline",
  "status": "COMPLETED",
  "durationMs": 154000,
  "stages": [
    {"name": "build", "status": "COMPLETED", "durationMs": 72000},
    {"name": "test", "status": "COMPLETED", "durationMs": 45000},
    {"name": "deploy", "status": "COMPLETED", "durationMs": 37000}
  ]
}
```

## Error Messages

All errors are printed to stderr with actionable guidance:

```
Error: Pipeline 'unknown-pipeline' not found
Hint: Run 'ci-engine-core pipeline list' to see available pipelines
```
