package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.config.ConfigurationLoader;
import com.hsbc.ci.engine.core.config.PipelineValidator;
import com.hsbc.ci.engine.core.model.PipelineDefinition;
import com.hsbc.ci.engine.core.utils.ConsoleOutput;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PipelineCommandTest {

    @Test
    void listOptionPrintsNoPipelinesMessage() {
        FakeConsole console = new FakeConsole();
        PipelineCommand command = new PipelineCommand();
        inject(command, "configurationLoader", new FakeConfigLoader(List.of(), Map.of()));
        inject(command, "pipelineOrchestrator", null);
        inject(command, "pipelineValidator", new FakePipelineValidator());
        inject(command, "console", console);

        CommandLine cmd = new CommandLine(command);
        int exit = cmd.execute("--list");

        assertEquals(PipelineCommand.EXIT_SUCCESS, exit);
        assertTrue(console.lines.stream().anyMatch(l -> l.contains("No pipelines")));
    }

    @Test
    void runWithoutNameSetsInvalidExit() {
        FakeConsole console = new FakeConsole();
        PipelineCommand command = new PipelineCommand();
        inject(command, "configurationLoader", new FakeConfigLoader(List.of(), Map.of()));
        inject(command, "pipelineOrchestrator", null);
        inject(command, "pipelineValidator", new FakePipelineValidator());
        inject(command, "console", console);

        CommandLine cmd = new CommandLine(command);
        cmd.execute("--run");
        assertEquals(PipelineCommand.EXIT_INVALID_DEFINITION, command.getExitCode());
        assertTrue(console.lines.stream().anyMatch(l -> l.contains("--name is required")));
    }

    @Test
    void validateMissingPipelineSetsInvalidExit() {
        FakeConsole console = new FakeConsole();
        PipelineCommand command = new PipelineCommand();
        inject(command, "configurationLoader", new FakeConfigLoader(List.of(), Map.of()));
        inject(command, "pipelineOrchestrator", null);
        inject(command, "pipelineValidator", new FakePipelineValidator());
        inject(command, "console", console);

        CommandLine cmd = new CommandLine(command);
        cmd.execute("--validate", "--name", "missing");

        assertEquals(PipelineCommand.EXIT_INVALID_DEFINITION, command.getExitCode());
        assertTrue(console.lines.stream().anyMatch(l -> l.contains("Pipeline not found")));
    }

    @Test
    void statusPrintsPipelineSummary() {
        FakeConsole console = new FakeConsole();
        Map<String, Object> pipeline = new HashMap<>();
        pipeline.put("description", "demo pipeline");
        pipeline.put("stages", List.of(Map.of("name", "build", "type", "build")));
        pipeline.put("environments", List.of("dev", "prod"));
        PipelineCommand command = new PipelineCommand();
        inject(command, "configurationLoader", new FakeConfigLoader(List.of("sample"), Map.of("sample", pipeline)));
        inject(command, "pipelineOrchestrator", null);
        inject(command, "pipelineValidator", new FakePipelineValidator());
        inject(command, "console", console);

        CommandLine cmd = new CommandLine(command);
        int exit = cmd.execute("--status", "--name", "sample");

        assertEquals(PipelineCommand.EXIT_SUCCESS, exit);
        assertTrue(console.lines.stream().anyMatch(l -> l.contains("Pipeline: sample")));
        assertTrue(console.lines.stream().anyMatch(l -> l.contains("demo pipeline")));
        assertTrue(console.lines.stream().anyMatch(l -> l.contains("build")));
        assertTrue(console.lines.stream().anyMatch(l -> l.contains("Environments")));
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            Field f = PipelineCommand.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class FakeConsole implements ConsoleOutput {
        final List<String> lines = new ArrayList<>();

        @Override
        public void print(String message) {
            lines.add(message);
        }

        @Override
        public void printError(String message) {
            lines.add(message);
        }

        @Override
        public void printf(String format, Object... args) {
            lines.add(String.format(format, args));
        }

        @Override
        public void printfError(String format, Object... args) {
            lines.add(String.format(format, args));
        }
    }

    private static class FakeConfigLoader extends ConfigurationLoader {
        private final Collection<String> pipelines;
        private final Map<String, Object> pipelineMap;

        FakeConfigLoader(Collection<String> pipelines, Map<String, Object> pipelineMap) {
            this.pipelines = pipelines;
            this.pipelineMap = pipelineMap;
        }

        @Override
        public Collection<String> listPipelines() {
            return pipelines;
        }

        @Override
        public Map<String, Object> getPipeline(String name) {
            return (Map<String, Object>) pipelineMap.get(name);
        }

        @Override
        public PipelineDefinition loadPipelineDefinition(String name) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FakePipelineValidator extends PipelineValidator {
        @Override
        public ValidationResult validate(PipelineDefinition pipeline) {
            return new ValidationResult(true, List.of());
        }
    }
}
