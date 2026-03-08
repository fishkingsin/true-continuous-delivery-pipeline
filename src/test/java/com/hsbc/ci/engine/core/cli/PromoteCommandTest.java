package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.model.Environment;
import com.hsbc.ci.engine.core.model.PromotionPolicy;
import com.hsbc.ci.engine.core.plugin.GateExecutor;
import com.hsbc.ci.engine.core.plugin.GateResult;
import com.hsbc.ci.engine.core.plugin.PluginManager;
import com.hsbc.ci.engine.core.utils.ConsoleOutput;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PromoteCommandTest {

    @Test
    void dryRunPrintsMessage() {
        FakeConsole console = new FakeConsole();
        PromoteCommand command = new PromoteCommand();
        inject(command, "environmentLoader", new FakeEnvLoader(Map.of()));
        inject(command, "gateExecutor", new FakeGateExecutor());
        inject(command, "console", console);
        inject(command, "dryRun", true);
        inject(command, "toEnv", "prod");

        command.run();

        assertTrue(console.lines.stream().anyMatch(l -> l.contains("[DRY RUN]")));
    }

    @Test
    void missingTargetEnvPrintsError() {
        FakeConsole console = new FakeConsole();
        PromoteCommand command = new PromoteCommand();
        inject(command, "environmentLoader", new FakeEnvLoader(Map.of()));
        inject(command, "gateExecutor", new FakeGateExecutor());
        inject(command, "console", console);
        inject(command, "toEnv", "prod");

        command.run();

        assertTrue(console.lines.stream().anyMatch(l -> l.contains("Target environment not found")));
    }

    @Test
    void requiredGateFailureBlocksPromotion() {
        FakeConsole console = new FakeConsole();
        FakeEnvLoader envLoader = new FakeEnvLoader(Map.of("prod", env("prod")));
        FakeGateExecutor gateExecutor = new FakeGateExecutor();
        gateExecutor.results.put("test-passed", GateResult.fail("nope"));

        PromoteCommand command = new PromoteCommand();
        inject(command, "environmentLoader", envLoader);
        inject(command, "gateExecutor", gateExecutor);
        inject(command, "console", console);
        inject(command, "toEnv", "prod");
        inject(command, "policy", "standard");

        command.run();

        assertTrue(console.lines.stream().anyMatch(l -> l.contains("[GATE FAILED]")));
        assertTrue(console.lines.stream().anyMatch(l -> l.contains("blocked")));
    }

    private Environment env(String name) {
        Environment e = new Environment();
        e.setName(name);
        return e;
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            var field = PromoteCommand.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FakeConsole implements ConsoleOutput {
        final List<String> lines = new ArrayList<>();

        @Override
        public void print(String message) { lines.add(message); }

        @Override
        public void printError(String message) { lines.add(message); }

        @Override
        public void printf(String format, Object... args) { lines.add(String.format(format, args)); }

        @Override
        public void printfError(String format, Object... args) { lines.add(String.format(format, args)); }
    }

    private static class FakeEnvLoader extends com.hsbc.ci.engine.core.config.EnvironmentLoader {
        private final Map<String, Environment> envs;

        FakeEnvLoader(Map<String, Environment> envs) {
            this.envs = envs;
        }

        @Override
        public Environment getEnvironment(String name) {
            if (name == null) {
                return null;
            }
            return envs.get(name);
        }

        @Override
        public List<Environment> getPromotionChain(String targetEnvironment) {
            Environment env = envs.get(targetEnvironment);
            if (env == null) {
                return List.of();
            }
            return List.of(env);
        }

        @Override
        public boolean shouldAutoPromote(String environmentName) {
            return false;
        }
    }

    private static class FakeGateExecutor extends GateExecutor {
        final Map<String, GateResult> results = new HashMap<>();

        FakeGateExecutor() { super(new PluginManager()); }

        @Override
        public GateResult executeGate(String gateType, Map<String, Object> config, com.hsbc.ci.engine.core.model.PipelineContext context) {
            return results.getOrDefault(gateType, GateResult.pass(gateType + " ok"));
        }
    }
}
