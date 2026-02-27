# Research: CI Engine Core CLI

**Date**: 2026-02-28  
**Feature**: CI Engine Core CLI - Enterprise Continuous Delivery Pipeline Tool

## Decisions Made

### Technology Stack

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Language | Java 21 | Current LTS with modern features, matches enterprise standards |
| Framework | Spring Boot 3.2.0 | Provides DI, auto-configuration, embedded server |
| CLI Framework | Picocli 4.7.5 | Native Java CLI, subcommand support, auto-help |
| Testing | JUnit Jupiter 5.10.0 | Standard Java testing, good IDE integration |
| Build Tool | Maven | Enterprise standard, CI/CD integration |
| YAML Processing | SnakeYAML | Standard YAML parsing for pipeline definitions |
| Logging | Log4j2 | Required by project |

### Pipeline Definition Format

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Format | YAML | Human-readable, widely used in CI/CD |
| Storage | File-based | No persistent storage - pipelines loaded from config/ |
| Execution Model | Sequential + Parallel | Depends on stage dependencies (dependsOn field) |

### Configuration Files

| Decision | Choice | Rationale |
|----------|--------|-----------|
| build.yml | Maven/Gradle/npm/dotnet | Multiple build tool support |
| checkout.yml | Git repos | Source code checkout configuration |
| ci-engine.yml | Global settings | Log level, timeouts, artifacts, notifications, credentials |
| deploy.yml | K8s/ECS | Deployment targets and strategies |
| environments.yml | Env definitions | Dev/staging/prod with gates and resources |
| plugins.yml | Extensions | Security scan, SonarQube, notifications, perf testing |
| promote.yml | Policies | Fast-track, standard, production, security-patch |

### Architecture Patterns

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Stage Execution | Strategy Pattern | Adding new stage types without modifying core |
| Plugin System | Interface-based | Extensibility for gates, notifications, custom stages |
| Orchestration | DAG-based | Sequential by default; parallel when dependsOn specified |
| Quality Gates | Plugin-based | Test-passed, coverage, security, performance gates |
| CLI Output | JSON + Human-readable | Machine parseable + user friendly |

## Alternatives Evaluated

### CLI Frameworks

- **Picocli** (chosen): Native Java, single JAR, no runtime deps
- **Spring Shell**: Requires Spring Boot, heavier
- **JCommander**: Less feature-rich for subcommands

### Pipeline Formats

- **YAML** (chosen): Industry standard (Jenkins, GitHub Actions)
- **JSON**: Less readable for complex pipelines
- **DSL (Groovy/Kotlin)**: Requires learning curve

### Storage

- **File-based** (chosen): No DB required, simple backup/restore
- **Database**: Overkill for CLI tool, adds deployment complexity

## Technical Notes

- Existing project scaffold provides core CLI commands
- Pipeline orchestrator handles stage execution
- Plugin system enables extensibility
- Exit codes align with Unix conventions (0 = success)
