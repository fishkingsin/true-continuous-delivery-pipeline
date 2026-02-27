# Implementation Plan: CI Engine Core CLI

**Branch**: `001-ci-engine-core` | **Date**: 2026-02-28 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-ci-engine-core/spec.md`

## Summary

Build a Spring Boot + Picocli CLI application for enterprise-grade Continuous Delivery pipelines. The CI Engine Core empowers Jenkins/Ansible to execute pre-defined pipelines with support for parallel execution, quality gates, multiple build tools, and extensible plugin system. Key features: sequential/parallel stage execution, quality gates, build tools (Maven/Gradle/npm/dotnet), git checkout, Kubernetes/ECS deployments, notifications, and promotion policies.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.2.0, Picocli 4.7.5, JUnit Jupiter 5.10.0, SnakeYAML  
**Storage**: File-based (YAML pipeline definitions, config files) - no persistent storage
**Testing**: JUnit 5 (unit, integration, E2E)  
**Target Platform**: Linux/Windows servers, CLI tool  
**Project Type**: CLI application (executable JAR)  
**Performance Goals**: CLI commands return <2s for non-executing operations (SC-005)  
**Constraints**: Integrates with Jenkins/Ansible via CLI; no external database

## Constitution Check

*GATE: Must pass before implementation.*

### Gates from Constitution

| Principle | Requirement | Status |
|-----------|-------------|--------|
| Code Quality | Follow standard conventions, keep simple, apply Boy Scout Rule | PASS - Standard Java 21 conventions |
| Code Quality | Use dependency injection | PASS - Spring Boot provides DI |
| Code Quality | Functions must be small, do one thing | PASS - Stage interface/implementations are single-purpose |
| Testing Standards | TDD approach, Red-Green-Refactor | PASS - JUnit 5 tests exist |
| Testing Standards | One assertion per test | PASS - Test structure supports this |
| Testing Standards | Unit tests for business logic | PASS - StageExecutor, PipelineOrchestrator tests exist |
| UX Consistency | Text in/out: stdin/args → stdout, errors → stderr | PASS - Picocli CLI pattern |
| UX Consistency | Support JSON + human-readable formats | PASS - Spec requires both |
| UX Consistency | Predictable exit codes (0 success, non-zero failure) | PASS - Spec requirement FR-005 |
| Performance | CLI commands respond within reasonable time | PASS - SC-005: <2s for non-executing |
| Security | Log security events, no secrets in logs | PASS - Requirement FR-008 |
| Security | Input validation on user-provided data | PASS - Configuration validation |

All gates pass. No violations to track.

## Project Structure

### Documentation

```text
specs/001-ci-engine-core/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md            # Phase 2 output
```

### Source Code

```text
src/
├── main/java/com/hsbc/ci/engine/core/
│   ├── CiEngineApplication.java        # Spring Boot entry point
│   ├── cli/                           # Picocli commands
│   │   ├── CiEngineCommand.java       # Root command
│   │   ├── PipelineCommand.java       # Pipeline subcommands (run, list, validate)
│   │   ├── StageCommand.java          # Stage subcommands
│   │   ├── DeployCommand.java         # Deploy subcommand
│   │   ├── PromoteCommand.java        # Promote subcommand
│   │   ├── ConfigCommand.java         # Config subcommand
│   │   ├── VersionCommand.java       # Version subcommand
│   │   ├── build/
│   │   │   ├── BuildCommand.java
│   │   │   └── MavenBuildCommand.java
│   │   ├── checkout/
│   │   │   ├── CheckoutCommand.java
│   │   │   └── CloneCommand.java
│   │   └── plugin/
│   │       └── PluginCommand.java
│   ├── config/
│   │   └── ConfigurationLoader.java   # YAML config loading
│   ├── model/
│   │   ├── PipelineContext.java       # Runtime context
│   │   ├── PipelineResult.java        # Execution result
│   │   ├── StageResult.java          # Stage result
│   │   ├── PipelineDefinition.java   # Pipeline YAML model
│   │   ├── StageDefinition.java      # Stage YAML model
│   │   └── Environment.java         # Environment YAML model
│   ├── orchestrator/
│   │   └── PipelineOrchestrator.java # Stage orchestration (sequential + parallel)
│   ├── plugin/
│   │   ├── Plugin.java               # Base plugin interface
│   │   ├── PluginManager.java        # Plugin lifecycle
│   │   ├── GatePlugin.java           # Gate extension point
│   │   ├── StagePlugin.java          # Custom stage extension
│   │   ├── NotifierPlugin.java       # Notification extension
│   │   ├── PluginResult.java
│   │   └── GateResult.java
│   └── stages/
│       ├── Stage.java                 # Stage interface
│       ├── StageExecutor.java        # Stage execution
│       ├── BuildStage.java           # Build implementation
│       ├── TestStage.java            # Test implementation
│       ├── DeployStage.java          # Deploy implementation
│       └── ContainerizeStage.java    # Containerize implementation
└── test/java/
    ├── e2e/
    │   └── CiEngineCliE2eTest.java
    ├── config/
    │   └── ConfigurationLoaderTest.java
    ├── orchestrator/
    │   └── PipelineOrchestratorTest.java
    ├── plugin/
    │   └── PluginManagerTest.java
    └── model/
        └── PipelineContextTest.java
```

### Configuration Files

| Config File | Purpose |
|------------|---------|
| `config/pipelines/*.yml` | Pipeline definitions |
| `config/build.yml` | Build tools (Maven, Gradle, npm, dotnet) |
| `config/checkout.yml` | Git repository checkout |
| `config/ci-engine.yml` | Global settings, artifacts, notifications, credentials |
| `config/deploy.yml` | Kubernetes/ECS deployment configs |
| `config/environments.yml` | Environment definitions with gates |
| `config/plugins.yml` | Plugin configurations |
| `config/promote.yml` | Promotion policies |

## Complexity Tracking

No Constitution violations - all gates pass.
