---

description: "Task list for CI Engine Core CLI implementation"
---

# Tasks: CI Engine Core CLI

**Input**: Design documents from `/specs/001-ci-engine-core/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project is already scaffolded - verify build and dependencies

- [ ] T001 Verify Maven build compiles successfully with mvn compile
- [ ] T002 [P] Verify all existing unit tests pass with mvn test
- [ ] T003 Verify JAR packaging works with mvn package -DskipTests

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that enables all user stories

- [ ] T004 Implement PipelineDefinition model in src/main/java/com/hsbc/ci/engine/core/model/PipelineDefinition.java
- [ ] T005 [P] Implement StageDefinition model in src/main/java/com/hsbc/ci/engine/core/model/StageDefinition.java
- [ ] T006 [P] Implement Environment model in src/main/java/com/hsbc/ci/engine/core/model/Environment.java
- [ ] T007 Implement PipelineValidator with YAML schema validation in src/main/java/com/hsbc/ci/engine/core/config/PipelineValidator.java
- [ ] T008 Add YAML pipeline loading to ConfigurationLoader in src/main/java/com/hsbc/ci/engine/core/config/ConfigurationLoader.java
- [ ] T009 [P] Create environment configuration directory structure config/environments/
- [ ] T010 Implement Gate model in src/main/java/com/hsbc/ci/engine/core/model/Gate.java

---

## Phase 3: User Story 1 - Execute CD Pipeline (Priority: P1) 🎯 MVP

**Goal**: Users can execute a pre-defined pipeline with visible progress, parallel execution, and proper exit codes

**Independent Test**: Run a pipeline with multiple stages (including parallel) and verify execution with status output

### Implementation for User Story 1

- [ ] T011 [P] [US1] Implement parallel stage execution in PipelineOrchestrator.execute() in src/main/java/com/hsbc/ci/engine/core/orchestrator/PipelineOrchestrator.java
- [ ] T012 [US1] Add stage dependency resolution (dependsOn) in PipelineOrchestrator for DAG-based execution
- [ ] T013 [US1] Implement fail-fast behavior (FR-009) when stage fails in PipelineOrchestrator
- [ ] T014 [US1] Add stage retry logic (FR-010) with configurable retry count in StageExecutor.java
- [ ] T015 [US1] Implement exit code handling in PipelineCommand.java - return 0 success, 1 failure, 2 invalid definition
- [ ] T016 [US1] Add progress output to PipelineCommand showing completed/running/pending stages
- [ ] T017 [US1] Add pipeline status command showing execution progress in PipelineCommand.java

### Tests for User Story 1

- [ ] T018 [P] [US1] Add unit test for sequential stage execution in tests/orchestrator/PipelineOrchestratorTest.java
- [ ] T019 [P] [US1] Add unit test for parallel stage execution (multiple stages with dependsOn)
- [ ] T020 [US1] Add unit test for fail-fast behavior when stage fails
- [ ] T021 [US1] Add unit test for stage retry logic in tests/stages/StageExecutorTest.java

**Checkpoint**: At this point, User Story 1 should be fully functional - pipelines can execute with parallel execution, progress reporting, proper exit codes, and fail-fast behavior

---

## Phase 4: User Story 2 - Validate Pipeline Definitions (Priority: P2)

**Goal**: Users can validate pipeline definitions without executing

**Independent Test**: Create a pipeline YAML and validate it returns success without running stages

### Implementation for User Story 2

- [ ] T022 [P] [US2] Implement pipeline validate subcommand in PipelineCommand.java
- [ ] T023 [P] [US2] Add validation for required fields (name, stages) per FR-003
- [ ] T024 [P] [US2] Add validation for stage types (build, test, deploy, containerize, checkout)
- [ ] T025 [US2] Add validation for stage dependency references (dependsOn)
- [ ] T026 [US2] Add support for stage timeout and retry validation
- [ ] T027 [US2] Add environment variable substitution in pipeline definitions

### Tests for User Story 2

- [ ] T028 [P] [US2] Add unit test for PipelineValidator in tests/config/PipelineValidatorTest.java
- [ ] T029 [P] [US2] Add unit test for invalid stage type detection
- [ ] T030 [US2] Add unit test for missing dependency reference detection

**Checkpoint**: At this point, User Story 2 is complete - pipeline definitions can be validated without execution

---

## Phase 5: User Story 3 - Configure Target Environments (Priority: P3)

**Goal**: Users can configure multiple deployment environments with promotion chain

**Independent Test**: Create environment configs and verify pipeline uses correct environment

### Implementation for User Story 3

- [ ] T031 [P] [US3] Implement environment configuration loading in src/main/java/com/hsbc/ci/engine/core/config/EnvironmentLoader.java
- [ ] T032 [P] [US3] Add environment variable override support (FR-007)
- [ ] T033 [US3] Add environment validation - check required fields exist
- [ ] T034 [US3] Add environment-specific variable substitution in pipeline context
- [ ] T035 [US3] Implement config command environment subcommands in ConfigCommand.java
- [ ] T036 [US3] Add environment promotion chain with auto-promote capability

### Tests for User Story 3

- [ ] T037 [P] [US3] Add unit test for EnvironmentLoader in tests/config/EnvironmentLoaderTest.java
- [ ] T038 [US3] Add integration test for environment override in pipeline execution

**Checkpoint**: At this point, User Story 3 is complete - environments can be configured and used in pipeline execution

---

## Phase 6: Quality Gates & Plugins

**Purpose**: Implement quality gates and plugin system

- [ ] T039 [P] Implement GateExecutor in src/main/java/com/hsbc/ci/engine/core/plugin/GateExecutor.java
- [ ] T040 [P] Implement test-passed gate in src/main/java/com/hsbc/ci/engine/core/plugin/gates/TestPassedGate.java
- [ ] T041 [P] Implement coverage-threshold gate in src/main/java/com/hsbc/ci/engine/core/plugin/gates/CoverageGate.java
- [ ] T042 Implement security-scan plugin integration in src/main/java/com/hsbc/ci/engine/core/plugin/gates/SecurityScanGate.java
- [ ] T043 Implement sonarqube plugin integration in src/main/java/com/hsbc/ci/engine/core/plugin/gates/SonarQubeGate.java
- [ ] T044 Add plugin configuration loading from config/plugins.yml in PluginManager.java

---

## Phase 7: Build Tools & Checkout

**Purpose**: Implement multi-build tool support and git checkout

- [ ] T045 [P] Add Maven build execution in BuildStage.java using mvn executable
- [ ] T046 [P] Add Gradle build execution in BuildStage.java using gradlew
- [ ] T047 [P] Add npm build execution in BuildStage.java
- [ ] T048 [P] Add dotnet build execution in BuildStage.java
- [ ] T049 Implement git clone in CloneCommand.java with configurable depth and target
- [ ] T049a Implement artifact registry configuration loading from ci-engine.yml in src/main/java/com/hsbc/ci/engine/core/config/ArtifactRegistryConfig.java
- [ ] T049b Add Docker registry support for artifact storage
- [ ] T049c Add S3 artifact storage configuration

---

## Phase 8: Deployment Targets

**Purpose**: Implement Kubernetes and ECS deployment

- [ ] T050 [P] Implement Kubernetes deployment in DeployStage.java with namespace support
- [ ] T051 [P] Implement ECS deployment in DeployStage.java with cluster support
- [ ] T052 Add rolling deployment strategy in src/main/java/com/hsbc/ci/engine/core/deploy/RollingStrategy.java
- [ ] T053 Add blue-green deployment strategy in src/main/java/com/hsbc/ci/engine/core/deploy/BlueGreenStrategy.java
- [ ] T054 Add canary deployment strategy in src/main/java/com/hsbc/ci/engine/core/deploy/CanaryStrategy.java

---

## Phase 9: Notifications & Credentials

**Purpose**: Implement notifications and credential management

- [ ] T055 [P] Implement Slack notification in src/main/java/com/hsbc/ci/engine/core/plugin/NotifierPlugin.java
- [ ] T056 [P] Implement Email notification in src/main/java/com/hsbc/ci/engine/core/plugin/NotifierPlugin.java
- [ ] T057 Implement credential management in src/main/java/com/hsbc/ci/engine/core/config/CredentialManager.java
- [ ] T058 Add support for Git, Docker, and AWS credentials

---

## Phase 10: Promotion Policies

**Purpose**: Implement promotion policies and manual approvals

- [ ] T059 Implement PromotionPolicy model in src/main/java/com/hsbc/ci/engine/core/model/PromotionPolicy.java
- [ ] T060 Implement manual approval gate in src/main/java/com/hsbc/ci/engine/core/plugin/gates/ManualApprovalGate.java
- [ ] T061 Implement fast-track, standard, production, security-patch policies in PromoteCommand.java

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T062 [P] Add JSON output format support for pipeline list and status commands (FR-011)
- [ ] T063 [P] Enhance help documentation for all CLI commands
- [ ] T064 Add logging configuration for pipeline operations (FR-008)
- [ ] T065 Add verbose mode support for debugging (--verbose flag)
- [ ] T066 Add webhook integration for CI tool notification (FR-012)
- [ ] T067 Run full test suite and fix any failures
- [ ] T068 Update quickstart.md with working pipeline examples

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - No dependencies on US1
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - No dependencies on US1/US2

### Within Each User Story

- Models before services
- Services before CLI commands
- Core implementation before tests
- Story complete before moving to next priority

### Parallel Opportunities

- Phase 1: T001-T003 can run in parallel
- Phase 2: T004-T006, T010 can run in parallel
- Phase 3 (US1): T011-T012 can run in parallel
- Phase 4 (US2): T022-T024 can run in parallel
- Phase 5 (US3): T031-T032 can run in parallel

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Add Phases 6-10 → Feature complete
6. Polish → Production ready

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
