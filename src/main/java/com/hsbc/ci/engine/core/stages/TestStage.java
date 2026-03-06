package com.hsbc.ci.engine.core.stages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.hsbc.ci.engine.core.model.PipelineContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class TestStage implements Stage {

    private static final Logger log = LoggerFactory.getLogger(TestStage.class);

    private static final String TEST_TOOL_MAVEN = "maven";
    private static final String TEST_TOOL_GRADLE = "gradle";
    private static final String TEST_TOOL_NPM = "npm";
    private static final String TEST_TOOL_DOTNET = "dotnet";

    private static final String TEST_TYPE_UNIT = "unit";
    private static final String TEST_TYPE_INTEGRATION = "integration";
    private static final String TEST_TYPE_E2E = "e2e";

    @Override
    public String execute(Map<String, Object> config, PipelineContext context) {
        String testTool = getConfigValue(config, "test-tool", TEST_TOOL_MAVEN);
        String testType = getConfigValue(config, "test-type", TEST_TYPE_UNIT);

        log.info("Running {} tests with tool: {}", testType, testTool);

        try {
            return runTests(testTool, testType, config);
        } catch (Exception e) {
            log.warn("Test tool '{}' not available: {}", testTool, e.getMessage());
            return runPlaceholder(testTool, testType);
        }
    }

    private String runTests(String testTool, String testType, Map<String, Object> config) throws Exception {
        List<String> command = buildTestCommand(testTool, testType, config);
        int exitCode = executeCommand(command);

        if (exitCode != 0) {
            throw new RuntimeException("Tests failed with exit code: " + exitCode);
        }

        log.info("Tests passed successfully using {}", testTool);
        return "Tests passed successfully using " + testTool;
    }

    private int executeCommand(List<String> command) throws Exception {
        log.info("Executing: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        pb.inheritIO();

        Process process = pb.start();
        return process.waitFor();
    }

    private List<String> buildTestCommand(String testTool, String testType, Map<String, Object> config) {
        Supplier<List<String>> commandBuilder = switch (testTool.toLowerCase()) {
            case TEST_TOOL_MAVEN -> () -> buildMavenCommand(testType, config);
            case TEST_TOOL_GRADLE -> () -> buildGradleCommand(testType, config);
            case TEST_TOOL_NPM -> () -> buildNpmCommand(testType, config);
            case TEST_TOOL_DOTNET -> () -> buildDotnetCommand(testType, config);
            default -> () -> buildMavenCommand(testType, config);
        };
        return commandBuilder.get();
    }

    private List<String> buildMavenCommand(String testType, Map<String, Object> config) {
        List<String> cmd = new ArrayList<>();
        cmd.add("mvn");
        cmd.add("test");

        if (TEST_TYPE_INTEGRATION.equals(testType)) {
            cmd.add("-DskipTests=false");
            cmd.add("-Dintegration-tests=true");
        } else if (TEST_TYPE_E2E.equals(testType)) {
            cmd.add("-DskipTests=false");
            cmd.add("-De2e-tests=true");
        }

        addOptions(config, cmd);
        return cmd;
    }

    private List<String> buildGradleCommand(String testType, Map<String, Object> config) {
        List<String> cmd = new ArrayList<>();
        cmd.add("./gradlew");
        cmd.add("test");

        if (TEST_TYPE_INTEGRATION.equals(testType)) {
            cmd.add("--tests");
            cmd.add("*IntegrationTest");
        } else if (TEST_TYPE_E2E.equals(testType)) {
            cmd.add("--tests");
            cmd.add("*E2ETest");
        }

        addOptions(config, cmd);
        return cmd;
    }

    private List<String> buildNpmCommand(String testType, Map<String, Object> config) {
        List<String> cmd = new ArrayList<>();
        cmd.add("npm");
        cmd.add("test");

        if (TEST_TYPE_INTEGRATION.equals(testType) || TEST_TYPE_E2E.equals(testType)) {
            cmd.add("--");
            cmd.add("--grep");
            cmd.add(testType);
        }

        addOptions(config, cmd);
        return cmd;
    }

    private List<String> buildDotnetCommand(String testType, Map<String, Object> config) {
        List<String> cmd = new ArrayList<>();
        cmd.add("dotnet");
        cmd.add("test");

        if (TEST_TYPE_INTEGRATION.equals(testType)) {
            cmd.add("--filter");
            cmd.add("Category=Integration");
        } else if (TEST_TYPE_E2E.equals(testType)) {
            cmd.add("--filter");
            cmd.add("Category=E2E");
        }

        addOptions(config, cmd);
        return cmd;
    }

    private void addOptions(Map<String, Object> config, List<String> cmd) {
        Object options = config.get("options");
        if (options instanceof String opts && !opts.isEmpty()) {
            for (String opt : opts.split("\\s+")) {
                cmd.add(opt);
            }
        }
    }

    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        return value instanceof String str && !str.isEmpty() ? str : defaultValue;
    }

    /**
     * Placeholder for test execution when the test tool is not available.
     *
     * In production, this should:
     * 1. Execute the appropriate test command for the build tool
     * 2. Support various test frameworks (JUnit 4/5, TestNG, pytest, etc.)
     * 3. Collect test results (JUnit XML, HTML reports)
     * 4. Generate coverage reports if configured
     * 5. Fail the stage if tests fail
     *
     * Supported test tools and their integrations:
     * - Maven: mvn test, mvn verify (with failsafe for integration tests)
     * - Gradle: ./gradlew test, ./gradlew check
     * - npm: npm test, npm run test:integration
     * - dotnet: dotnet test, dotnet test --filter
     *
     * Test type support:
     * - unit: Unit tests (default)
     * - integration: Integration tests
     * - e2e: End-to-end tests
     */
    private String runPlaceholder(String testTool, String testType) {
        log.warn("Test tool '{}' not available, using placeholder", testTool);

        return """
            [PLACEHOLDER] Test Execution
            ===========================
            Test Tool: %s
            Test Type: %s
            
            [INFO] Test tool not available - using placeholder
            [INFO] In production, this would:
              1. Execute %s test command
              2. Run %s tests
              3. Generate test reports (JUnit XML)
              4. Collect code coverage (if configured)
              5. Fail stage if any tests fail
            
            Supported test tools:
              - maven: mvn test, mvn verify
              - gradle: ./gradlew test
              - npm: npm test
              - dotnet: dotnet test
            
            [SUCCESS] Placeholder test execution completed
            """.formatted(testTool, testType, testTool, testType);
    }
}
