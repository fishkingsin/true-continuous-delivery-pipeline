# Per-Environment Properties Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Provide per-environment Spring Boot properties (dev/test/staging/prod) covering server port, logging level, datasource URL/credentials, request timeouts, HTTP proxy settings, and retry defaults.

**Architecture:** Use Spring Boot profile-specific property files (`application-<profile>.properties`) so profile activation selects the right configuration. Keep values in property files (no code constants), and expose them via `@ConfigurationProperties` beans for typed access in code/tests. Default profile remains `dev` unless overridden.

**Tech Stack:** Java 21, Spring Boot 3.2.x, JUnit 5, Spring Test.

---

### Task 1: Capture plan in repo
**Files:** Create `docs/plans/2026-03-08-env-profiles.md`
- Step 1: Write the plan content above into the file.
- Step 2: `git add docs/plans/2026-03-08-env-profiles.md`
- Step 3: `git status` to confirm only the plan is staged.

### Task 2: Define profile-specific property files
**Files:** Create `src/main/resources/application-dev.properties`, `application-test.properties`, `application-staging.properties`, `application-prod.properties`
- Step 1: Add keys for `server.port`, `logging.level.root`, `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`, `app.http.timeout.connect`, `app.http.timeout.read`, `app.http.proxy.host`, `app.http.proxy.port`, `app.http.proxy.non-proxy-hosts`, `app.http.retry.attempts`, `app.http.retry.backoff`.
- Step 2: Use provided per-profile values; if absent, fill sensible defaults (dev: 8080/DEBUG/local DB; test: 0/WARN/in-memory or stub; staging: 8082/INFO/staging DB; prod: 8080/INFO/prod DB; proxy/retry/timeouts per requirements).
- Step 3: `git add src/main/resources/application-*.properties`
- Step 4: `git status` to confirm the new files are staged.

### Task 3: Bind properties to typed config
**Files:** Create `src/main/java/com/hsbc/ci/engine/core/config/HttpClientProperties.java`; modify `src/main/java/com/hsbc/ci/engine/core/config/` package as needed (e.g., `ConfigurationLoader` not required to change)
- Step 1: Add `@ConfigurationProperties(prefix = "app.http")` class with fields `Timeout timeout`, `Proxy proxy`, `Retry retry` (inner static classes).
- Step 2: Enable configuration properties in your Spring Boot entrypoint/config (`@EnableConfigurationProperties(HttpClientProperties.class)`), locating the main application class (likely under `src/main/java/com/hsbc/ci/engine/core`).
- Step 3: If no entrypoint exists, add a minimal `@Configuration` class `PropertyConfig` in the same package to register the properties bean.
- Step 4: `git add src/main/java/.../HttpClientProperties.java` and any modified config class.

### Task 4: Tests for profile property loading
**Files:** Create `src/test/java/com/hsbc/ci/engine/core/config/HttpClientPropertiesTest.java`
- Step 1: Write Spring Boot slice test using `@SpringBootTest(classes = {PropertyConfig.class, HttpClientProperties.class})` and `@ActiveProfiles("<profile>")` to load each profile.
- Step 2: For each profile (dev/test/staging/prod), assert mapped values equal the expected file values (port, logging level, datasource URL/user/pass, timeouts, proxy, retry).
- Step 3: Run `mvn -q -Dtest=HttpClientPropertiesTest test` and expect FAIL if mappings/values differ.
- Step 4: Adjust property files or bindings until tests pass.
- Step 5: `git add src/test/java/com/hsbc/ci/engine/core/config/HttpClientPropertiesTest.java`

### Task 5: Optional: Wire properties into HTTP client usage
**Files:** Locate HTTP client creation (if present; search for `HttpClient`, `WebClient`, `RestTemplate`)
- Step 1: If an HTTP client exists, inject `HttpClientProperties` and apply timeouts/proxy/retry settings.
- Step 2: Add/adjust unit tests around that client to assert the configuration is applied.
- Step 3: `git add` modified files and tests.

### Task 6: Validate CLI/server profile selection
**Files:** If a Spring Boot main class exists, ensure `application.properties` (or CLI args) sets a sane default profile (e.g., `spring.profiles.active=dev` for local) and document how to override (`--spring.profiles.active=staging`).
- Step 1: Add/adjust `src/main/resources/application.properties` default profile if needed.
- Step 2: Add a short doc note in `README.md` or `docs/plans/2026-03-08-cli-coverage.md` explaining profile usage.
- Step 3: `git add` changed files.

### Task 7: Full test run
**Files:** n/a
- Step 1: `mvn -q test`
- Step 2: Confirm green; investigate failures if any.

### Task 8: Commit
**Files:** n/a
- Step 1: `git status` ensure only intended files staged.
- Step 2: `git commit -m "feat: add profile-specific config properties"` (or similar).

---

## How Application Properties Are Effective

### Profile-Specific Property Files

Spring Boot automatically loads `application-<profile>.properties` when a profile is active:

| Profile | File | Default Values |
|---------|------|----------------|
| dev | `application-dev.properties` | port 8080, DEBUG logging, local DB |
| test | `application-test.properties` | port 8080, WARN logging, test DB |
| staging | `application-staging.properties` | port 8082, INFO logging, staging DB |
| prod | `application-prod.properties` | port 8080, INFO logging, prod DB |

### Activating a Profile

**Option 1: Command line argument**
```bash
java -jar ci-engine.jar --spring.profiles.active=staging
```

**Option 2: Environment variable**
```bash
export SPRING_PROFILES_ACTIVE=staging
java -jar ci-engine.jar
```

**Option 3: In application.properties (default)**
```properties
spring.profiles.active=dev
```

### Property Precedence (highest to lowest)

1. Command line arguments (`--spring.profiles.active=...`)
2. Environment variables (`SPRING_PROFILES_ACTIVE`)
3. Profile-specific file (`application-<profile>.properties`)
4. Base file (`application.properties`)

### Accessing HTTP Client Properties in Code

Inject `HttpClientProperties` wherever needed:

```java
@Autowired
private HttpClientProperties httpProps;

public void configureClient(HttpClient client) {
    client.connectTimeout(httpProps.getTimeout().getConnect());
    client.readTimeout(httpProps.getTimeout().getRead());
    // ... proxy and retry settings
}
```

### Current Placeholder Values

The property files contain placeholder values that should be replaced with real configuration before deployment:

- **Dev**: Uses local/dev values suitable for local development
- **Test**: Uses minimal timeouts and test proxy settings
- **Staging**: Uses staging environment values
- **Prod**: Uses production-grade timeouts and retry settings

Replace placeholders in each `application-<profile>.properties` file before deploying to that environment.
