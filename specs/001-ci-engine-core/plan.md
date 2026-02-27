# Implementation Plan: CI Engine Core CLI

**Branch**: `001-ci-engine-core` | **Date**: 2026-02-27 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-ci-engine-core/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Build a Spring Boot + Picocli CLI application for enterprise-grade Continuous Delivery pipelines. The CI Engine Core empowers Jenkins/Ansible to execute pre-defined pipelines without DevOps writing project-specific scripts.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.2.0, Picocli 4.7.5, JUnit Jupiter 5.10.0  
**Storage**: File-based (YAML pipeline definitions, JSON configuration)  
**Testing**: JUnit 5 (unit, integration, E2E)  
**Target Platform**: Linux/Windows servers, CLI tool  
**Project Type**: CLI application (executable JAR)  
**Performance Goals**: CLI commands return <2s for non-executing operations  
**Constraints**: No external database required; integrates with Jenkins/Ansible  
**Scale/Scope**: Single CLI tool, supports enterprise multi-team usage

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Gates from Constitution

| Principle         | Requirement                                                    | Status                                                    |
| ----------------- | -------------------------------------------------------------- | --------------------------------------------------------- |
| Code Quality      | Follow standard conventions, keep simple, apply Boy Scout Rule | PASS - Standard Java conventions, Maven project structure |
| Code Quality      | Use dependency injection                                       | PASS - Spring Boot provides DI                            |
| Code Quality      | Functions must be small, do one thing                          | PASS - Stage classes are single-purpose                   |
| Testing Standards | TDD approach, Red-Green-Refactor                               | PASS - JUnit 5 tests exist in src/test                    |
| Testing Standards | One assertion per test                                         | PASS - Test structure supports this                       |
| Testing Standards | Unit tests for business logic                                  | PASS - StageExecutor, PipelineOrchestrator tests          |
| UX Consistency    | Text in/out: stdin/args → stdout, errors → stderr              | PASS - Picocli CLI pattern                                |
| UX Consistency    | Support JSON + human-readable formats                          | PASS - Spec requires both                                 |
| UX Consistency    | Predictable exit codes (0 success, non-zero failure)           | PASS - Spec requirement FR-005                            |
| Performance       | CLI commands respond within reasonable time                    | PASS - SC-005: <2s for non-executing                      |
| Security          | Log security events, no secrets in logs                        | PASS - Requirement FR-008                                 |
| Security          | Input validation on user-provided data                         | PASS - Configuration validation                           |

### Constitution Compliance

All gates pass. No violations to track.

## Project Structure

### Documentation (this feature)

```text
specs/001-ci-engine-core/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/
├── main/java/com/hsbc/ci/engine/core/
│   ├── cli/                    # Picocli commands
│   │   ├── CiEngineCommand.java
│   │   ├── PipelineCommand.java
│   │   ├── StageCommand.java
│   │   ├── DeployCommand.java
│   │   ├── PromoteCommand.java
│   │   ├── ConfigCommand.java
│   │   ├── VersionCommand.java
│   │   ├── build/
│   │   ├── checkout/
│   │   └── plugin/
│   ├── config/
│   │   └── ConfigurationLoader.java
│   ├── model/
│   │   ├── PipelineContext.java
│   │   ├── PipelineResult.java
│   │   └── StageResult.java
│   ├── orchestrator/
│   │   └── PipelineOrchestrator.java
│   ├── plugin/
│   │   ├── Plugin.java
│   │   ├── PluginManager.java
│   │   ├── GatePlugin.java
│   │   ├── StagePlugin.java
│   │   ├── NotifierPlugin.java
│   │   └── ...
│   ├── stages/
│   │   ├── Stage.java
│   │   ├── StageExecutor.java
│   │   ├── BuildStage.java
│   │   ├── TestStage.java
│   │   ├── DeployStage.java
│   │   └── ContainerizeStage.java
│   └── CiEngineApplication.java
└── test/java/
    ├── e2e/
    │   └── CiEngineCliE2eTest.java
    ├── config/
    │   └── ConfigurationLoaderTest.java
    ├── orchestrator/
    │   └── PipelineOrchestratorTest.java
    ├── plugin/
    │   ├── PluginManagerTest.java
    │   └── ...
    └── model/
        ├── PipelineContextTest.java
        ├── PipelineResultTest.java
        ├── StageResultTest.java
        └── ...
```

**Structure Decision**: Spring Boot + Picocli CLI application. Single project with standard Maven structure. Already scaffolded with CLI commands, models, orchestrator, and plugin system.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations - all Constitution gates pass.
