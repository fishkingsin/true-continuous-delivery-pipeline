# CLI Coverage Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Raise coverage for CLI entrypoints (Picocli commands) and surrounding behaviors, ensuring key modes/options are verified end-to-end.

**Architecture:** Add focused unit/integration tests around Picocli commands, stubbing dependencies where appropriate via Spring test context; exercise help/validation/option flows and error paths; prefer real ConsoleOutput over mocks when feasible.

**Tech Stack:** Java 21, Spring Boot 3, JUnit Jupiter 5, Picocli.

---

### Task 1: Document current CLI coverage baseline

**Files:**
- Read: `src/test/java/com/hsbc/ci/engine/core/cli/PipelineCommandTest.java`
- Read: `src/test/java/com/hsbc/ci/engine/core/e2e/CiEngineCliE2eTest.java`

**Step 1: Capture current coverage numbers (CLI packages)**

- Run: `mvn -q -DskipITs=false -Dtest='*Cli*Test,*CommandTest' test`
- Note: If a coverage plugin exists, run it; otherwise note observed gaps from reports/logs.

**Step 2: Summarize gaps**

- Identify missing option paths (help/validation errors, success paths).

**Step 3: Commit?** Not applicable; this is baseline collection.

### Task 2: Add CLI help/usage coverage for Root command

**Files:**
- Test: `src/test/java/com/hsbc/ci/engine/core/cli/CiEngineCommandTest.java` (new)
- Prod read: `src/main/java/com/hsbc/ci/engine/core/cli/CiEngineCommand.java`

**Step 1: Write failing test**

- Use Picocli `CommandLine` with `RootCommand` to assert `--help` and no-args print usage without exceptions; assert exit code 0.

**Step 2: Run test to see it fail**

- Run: `mvn -q -Dtest=CiEngineCommandTest test`
- Expect fail due to missing test file (red).

**Step 3: Implement minimal test code**

- Add test covering:
  - `--help` shows description/footer substring.
  - No args prints usage; exit code is 0.

**Step 4: Run test to pass**

- Run: `mvn -q -Dtest=CiEngineCommandTest test`

**Step 5: Commit**

- `git add src/test/java/com/hsbc/ci/engine/core/cli/CiEngineCommandTest.java`
- `git commit -m "test: cover root CLI help usage"`

### Task 3: Strengthen PipelineCommand behaviors (list/run/validate/status error paths)

**Files:**
- Modify: `src/test/java/com/hsbc/ci/engine/core/cli/PipelineCommandTest.java`
- Read: `src/main/java/com/hsbc/ci/engine/core/cli/PipelineCommand.java`

**Step 1: Write failing tests**

- Cases:
  - `--list` prints header when no pipelines.
  - `--run` without `--name` throws `IllegalArgumentException` with message `--name is required`.
  - `--validate` with missing pipeline throws `IllegalArgumentException("Pipeline not found: ...")`.
  - `--status` renders summary when pipeline map contains description/stages/environments (can stub loader with simple map).
- Use lightweight fakes for `ConfigurationLoader`, `PipelineOrchestrator`, `PipelineValidator`, `ConsoleOutput` (can inject via constructor or reflection; or instantiate command and set fields).

**Step 2: Run tests to see expected failures**

- `mvn -q -Dtest=PipelineCommandTest test`

**Step 3: Implement minimal test updates**

- Add tests per above behaviors; avoid heavy Spring context (plain unit preferred).

**Step 4: Run tests green**

- `mvn -q -Dtest=PipelineCommandTest test`

**Step 5: Commit**

- `git add src/test/java/com/hsbc/ci/engine/core/cli/PipelineCommandTest.java`
- `git commit -m "test: increase pipeline command coverage"`

### Task 4: Cover PromoteCommand dry-run and gate invocation

**Files:**
- Test: `src/test/java/com/hsbc/ci/engine/core/cli/PromoteCommandTest.java` (new)
- Prod read: `src/main/java/com/hsbc/ci/engine/core/cli/PromoteCommand.java`

**Step 1: Write failing tests**

- Cases:
  - Dry-run prints `[DRY RUN] Would perform promotion`.
  - Missing target env prints `[ERROR] Target environment not found: ...`.
  - Required gates iterates and prints pass/fail (stub GateExecutor to return pass once, fail once).
- Build command instance with stubbed `EnvironmentLoader`, `GateExecutor`, `ConsoleOutput`.

**Step 2: Run test to fail**

- `mvn -q -Dtest=PromoteCommandTest test`

**Step 3: Add minimal stubs/tests**

- Implement stubs inside test or simple inner classes; assert console output contains expected markers.

**Step 4: Run tests – current status**

- `mvn -q -Dtest=PromoteCommandTest test`
- **Outcome (2026-03-08): failing**
  - `dryRunPrintsMessage` error: NPE in `FakeEnvLoader#getEnvironment` (plan: guard missing env in fake or ensure map contains key)
  - `requiredGateFailureBlocksPromotion` error: reflection injection sets `policy` as `String`; adjust to accept `PromotionPolicy` or set via constructor

**Step 5: Fix and re-run**

- Update test fakes to avoid NPE and set policy correctly, then rerun until green.

**Step 6: Commit**

- `git add src/test/java/com/hsbc/ci/engine/core/cli/PromoteCommandTest.java`
- `git commit -m "test: cover promote command behaviors"`

### Task 5: Cover VersionCommand output

**Files:**
- Test: `src/test/java/com/hsbc/ci/engine/core/cli/VersionCommandTest.java` (new)
- Prod read: `src/main/java/com/hsbc/ci/engine/core/cli/VersionCommand.java`

**Step 1: Write failing test**

- Assert `run()` prints version string `1.0.0` to `ConsoleOutput`.

**Step 2: Run test to fail**

- `mvn -q -Dtest=VersionCommandTest test`

**Step 3: Implement minimal test**

- Use stub ConsoleOutput capturing lines; assert captured contains `1.0.0`.

**Step 4: Run tests green**

- `mvn -q -Dtest=VersionCommandTest test`

**Step 5: Commit**

- `git add src/test/java/com/hsbc/ci/engine/core/cli/VersionCommandTest.java`
- `git commit -m "test: add version command coverage"`

### Task 6: Cover StageCommand placeholder behavior

**Files:**
- Test: `src/test/java/com/hsbc/ci/engine/core/cli/StageCommandTest.java` (new)
- Prod read: `src/main/java/com/hsbc/ci/engine/core/cli/StageCommand.java`

**Step 1: Write failing test**

- Assert `run()` prints “Stage command not yet implemented”.

**Step 2: Run test to fail**

- `mvn -q -Dtest=StageCommandTest test`

**Step 3: Add minimal test**

- Stub ConsoleOutput capture, assert message.

**Step 4: Run tests green**

- `mvn -q -Dtest=StageCommandTest test`

**Step 5: Commit**

- `git add src/test/java/com/hsbc/ci/engine/core/cli/StageCommandTest.java`
- `git commit -m "test: cover stage command placeholder"`

### Task 7: Sanity e2e invocation of CiEngineCommand wiring (argument dispatch)

**Files:**
- Test: `src/test/java/com/hsbc/ci/engine/core/cli/CiEngineCommandDispatchTest.java` (new)
- Prod read: `src/main/java/com/hsbc/ci/engine/core/cli/CiEngineCommand.java`

**Step 1: Write failing test**

- Create `CommandLine` with `RootCommand` + subcommands (can instantiate new StageCommand/VersionCommand/PromoteCommand with stubbed dependencies as needed) and assert calling `execute("version")` returns 0 and triggers output; calling unknown command triggers `UnmatchedArgumentException` handled via picocli (or expect non-zero exit).

**Step 2: Run test to fail**

- `mvn -q -Dtest=CiEngineCommandDispatchTest test`

**Step 3: Implement minimal wiring test**

- Focus on dispatch return codes and that `version` route executes.

**Step 4: Run tests green**

- `mvn -q -Dtest=CiEngineCommandDispatchTest test`

**Step 5: Commit**

- `git add src/test/java/com/hsbc/ci/engine/core/cli/CiEngineCommandDispatchTest.java`
- `git commit -m "test: add cli dispatch coverage"`

### Task 8: Optional – narrow E2E smoke via existing CiEngineCliE2eTest (keep lightweight)

**Files:**
- Modify: `src/test/java/com/hsbc/ci/engine/core/e2e/CiEngineCliE2eTest.java` if quick win exists (e.g., add a simple `--help` invocation).

**Step 1: Write failing addition**

- Add a small case for `ci-engine --help` output contains title/footer.

**Step 2: Run specific test**

- `mvn -q -Dtest=CiEngineCliE2eTest test`

**Step 3: Keep green**

- Ensure it doesn’t require external resources.

### Task 9: Run full CLI-focused suite

**Step 1: Execute CLI tests**

- `mvn -q -Dtest='*Cli*Test,*CommandTest' test`

**Step 2: Verify all green and capture coverage summary**

- If JaCoCo configured, inspect `target/site/jacoco/index.html` and note CLI package coverage.

**Step 3: Commit aggregated changes (if batching)**

- If tasks were not committed individually, commit now with message reflecting coverage increase.

### Task 10: Update plan status

**Files:**
- Update: `docs/plans/2026-03-08-cli-coverage.md` (this plan)

**Step 1: Mark tasks done as executed.**

**Step 2: Commit plan if modified**

- `git add docs/plans/2026-03-08-cli-coverage.md`
- `git commit -m "docs: add cli coverage plan"` (if not yet committed)

### Testing Strategy (summary)

- Unit-style tests with stubbed dependencies for commands to avoid heavy Spring context.
- Assert console outputs and exit codes for each option/error path.
- Light E2E invocation for dispatch sanity; avoid external calls.
- Keep tests deterministic and side-effect free (no real file/network).

### Integration Points / Considerations

- Use stub ConsoleOutput capturing strings.
- Picocli `CommandLine` can be built around command instances to execute subcommands directly.
- Ensure existing logging doesn’t interfere with assertions (assert on captured output only).
- Respect TDD: each test red -> green -> refactor; minimal production changes anticipated (mostly tests).
