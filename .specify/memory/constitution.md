# CI Engine Core Constitution

## Core Principles

### I. Code Quality (Clean Code)
Code is clean if it can be understood easily by everyone on the team. Clean code can be read and enhanced by a developer other than its original author.

**General Rules:**
- Follow standard conventions and keep it simple (simpler is always better)
- Apply the Boy Scout Rule: leave the campground cleaner than you found it
- Always find and address the root cause of problems

**Design Rules:**
- Keep configurable data at high levels
- Prefer polymorphism to if/else or switch/case
- Separate multi-threading code from business logic
- Prevent over-configurability
- Use dependency injection
- Follow Law of Demeter: classes should know only their direct dependencies

**Naming & Functions:**
- Use descriptive, unambiguous, pronounceable, and searchable names
- Make meaningful distinctions; avoid encodings or magic numbers
- Functions must be small, do one thing, and have no side effects
- Prefer fewer arguments; avoid flag arguments

**Comments & Structure:**
- Explain yourself in code first; use comments only for intent, clarification, or warnings
- Don't comment out code—remove it
- Separate concepts vertically; declare variables close to their usage
- Keep lines short; use white space to associate related things

### II. Testing Standards
Tests are first-class artifacts that must be maintained with the same rigor as production code.

**Mandatory Requirements:**
- One assertion per test for clarity and debuggability
- Tests MUST be readable, fast, independent, and repeatable
- TDD approach: write tests first, verify they fail, then implement
- Red-Green-Refactor cycle strictly enforced for new features

**Test Coverage Areas:**
- Unit tests for all business logic
- Integration tests for library contracts and inter-service communication
- Contract tests for API/CLI interfaces

### III. User Experience Consistency
Every interaction with the CLI must be predictable, consistent, and user-friendly.

**Interface Standards:**
- Text in/out protocol: stdin/args → stdout, errors → stderr
- Support both JSON and human-readable formats
- Consistent command structure across all commands
- Clear, actionable error messages with suggested remediation

**Output Standards:**
- Consistent formatting and output patterns
- Predictable exit codes (0 for success, non-zero for errors)
- Progress indicators for long-running operations
- Verbose mode available for debugging

### IV. Performance Requirements
Systems must be designed for efficiency and scalability.

**Performance Mandates:**
- CLI commands must respond within reasonable time bounds
- Memory usage must remain bounded regardless of input size
- Efficient resource handling—release resources promptly
- Benchmark performance-critical paths

**Optimization Principles:**
- Start simple; follow YAGNI (You Aren't Gonna Need It)
- Profile before optimizing; optimize the critical path
- Cache appropriately; invalidate cache deliberately

## Security & Reliability

- All security-related events MUST be logged
- No secrets in logs or error messages
- Input validation on all user-provided data
- Graceful degradation with clear error messages

## Development Workflow

- All PRs/reviews must verify compliance with constitution principles
- Complexity must be justified with documented rationale
- Use docs/ for runtime development guidance
- Tests must pass before merging

## Governance

**Amendment Procedure:**
- Constitution supersedes all other practices
- Amendments require documentation, approval, and migration plan
- Version bumps follow semantic versioning:
  - MAJOR: Backward-incompatible governance changes
  - MINOR: New principles or materially expanded guidance
  - PATCH: Clarifications, wording, typo fixes

**Compliance Review:**
- Every feature implementation must demonstrate alignment with constitution
- Constitution Check is a mandatory gate before implementation begins

**Version**: 1.0.0 | **Ratified**: 2026-02-27 | **Last Amended**: 2026-02-27
