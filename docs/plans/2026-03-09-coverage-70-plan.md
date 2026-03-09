# Coverage 70% Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Raise overall line coverage from 65.3% to at least 70% by adding targeted tests starting with zero-coverage classes.

**Architecture:** Add focused unit tests that isolate external effects via fakes/mocks (no real network/process calls). Prefer refactor-to-inject for testability where needed; otherwise use overridable factories. Validate current placeholder behavior rather than future functionality.

**Tech Stack:** Java 21, JUnit 5, Mockito/AssertJ (if already present), Picocli test harness, JaCoCo.

---

### Task 1: Cover JsonOutput (0% class)

**Files:**
- Create: `src/test/java/com/hsbc/ci/engine/core/utils/JsonOutputTest.java`
- Read: `src/main/java/com/hsbc/ci/engine/core/utils/JsonOutput.java`

**Steps:**
1) Write tests for `toJson(null)` and empty map → `{}`.
2) Test escaping: keys/values with quotes, backslashes, newline, tab.
3) Test number/boolean serialization.
4) Test nested map and iterable serialization.
5) Test fallback to `toString()` for unknown object types.
6) Run `mvn -Dtest=JsonOutputTest test`.

### Task 2: Cover Notifiers (EmailNotifier, SlackNotifier) 0%

**Files:**
- Create: `src/test/java/com/hsbc/ci/engine/core/plugin/notifiers/EmailNotifierTest.java`
- Create: `src/test/java/com/hsbc/ci/engine/core/plugin/notifiers/SlackNotifierTest.java`
- Read/consider light refactor (if needed for testability):
  - `src/main/java/com/hsbc/ci/engine/core/plugin/notifiers/EmailNotifier.java`
  - `src/main/java/com/hsbc/ci/engine/core/plugin/notifiers/SlackNotifier.java`

**Steps:**
1) Email init defaults: smtpHost/port/from; respects provided values.
2) Email notify skips when `to` missing; logs warn (assert via logger spy or absence of exception).
3) Email body composition: includes title/level/message/metadata, safe substring.
4) Slack notify skips when webhook missing/empty; logs warn.
5) Slack message color mapping: ERROR→#ff0000, WARN→#ffa500, default→#36a64f; escaping works.
6) For HTTP send, avoid real network: inject/mock HttpClient via refactor (protected factory or constructor injection). Cover 200 OK vs non-200 vs exception.
7) Run `mvn -Dtest=*NotifierTest test`.

### Task 3: Cover Deploy Strategies (BlueGreen, Canary, Rolling) 0%

**Files:**
- Create: `src/test/java/com/hsbc/ci/engine/core/deploy/BlueGreenStrategyTest.java`
- Create: `src/test/java/com/hsbc/ci/engine/core/deploy/CanaryStrategyTest.java`
- Create: `src/test/java/com/hsbc/ci/engine/core/deploy/RollingStrategyTest.java`
- Read/possible small refactor for injection:
  - `src/main/java/com/hsbc/ci/engine/core/deploy/BlueGreenStrategy.java`
  - `src/main/java/com/hsbc/ci/engine/core/deploy/CanaryStrategy.java`
  - `src/main/java/com/hsbc/ci/engine/core/deploy/RollingStrategy.java`

**Steps:**
1) Introduce overridable factory/wrapper for `ProcessBuilder` or reuse `ProcessExecutor` with fakes to avoid real kubectl.
2) BlueGreen: happy path returns prepared message; non-zero exit returns failure; exception path returns failure.
3) Canary: invalid traffic (<0 or >100) short-circuits with failure; valid path success; non-zero exit failure; exception path.
4) Rolling: success path, rollout timeout/non-zero exit path, exception path.
5) Run `mvn -Dtest=*StrategyTest test`.

### Task 4: Cover Plugin Stages (low line/branch)

**Files:**
- Create: `src/test/java/com/hsbc/ci/engine/core/plugin/stages/SecurityScanStageTest.java`
- Create: `src/test/java/com/hsbc/ci/engine/core/plugin/stages/SonarQubeStageTest.java`
- Read: `src/main/java/com/hsbc/ci/engine/core/plugin/stages/SecurityScanStage.java`, `.../SonarQubeStage.java`

**Steps:**
1) Verify placeholder warnings are emitted and result is success/failure depending on tool presence flag (simulate with env/config/mocked executor per implementation).
2) Cover branches for missing tool vs present tool paths.
3) Run `mvn -Dtest=*StageTest test`.

### Task 5: Cover CLI Build/Checkout Commands (low branch/line)

**Files:**
- Create: `src/test/java/com/hsbc/ci/engine/core/cli/build/BuildCommandTest.java`
- Create: `src/test/java/com/hsbc/ci/engine/core/cli/checkout/CheckoutCommandTest.java`
- Read: corresponding command classes and services they call.

**Steps:**
1) Use Picocli `CommandLine` to parse required options; assert validation failures when missing.
2) Mock underlying services to simulate success and failure; assert exit codes/messages.
3) Cover flags (e.g., dry-run/verbose) branches if present.
4) Run `mvn -Dtest=*CommandTest test`.

### Task 6: Cover ProcessExecutor branches (if time permits)

**Files:**
- Create: `src/test/java/com/hsbc/ci/engine/core/utils/ProcessExecutorTest.java`
- Read: `src/main/java/com/hsbc/ci/engine/core/utils/ProcessExecutor.java`

**Steps:**
1) Fake `Process` to simulate: exit 0, non-zero exit, IOException, InterruptedException.
2) Assert outputs and error handling paths.
3) Run `mvn -Dtest=ProcessExecutorTest test`.

### Task 7: Full verification

**Steps:**
1) Run full suite: `mvn test`.
2) Check `target/site/jacoco/index.html` and confirm line coverage ≥70%.
3) If short, add focused assertions where gaps remain.
