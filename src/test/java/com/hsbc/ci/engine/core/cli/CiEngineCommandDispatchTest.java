package com.hsbc.ci.engine.core.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;

class CiEngineCommandDispatchTest {

    @Test
    void dispatchesToVersionCommand() {
        CapturingConsole console = new CapturingConsole();
        VersionCommand version = new VersionCommand();
        inject(version, "console", console);

        CommandLine cmd = new CommandLine(new CiEngineCommand.RootCommand())
            .addSubcommand("version", version);

        int exit = cmd.execute("version");

        assertEquals(0, exit);
        assertTrue(console.last.contains("1.0.0"));
    }

    @Test
    void unknownCommandReturnsNonZero() {
        CommandLine cmd = new CommandLine(new CiEngineCommand.RootCommand());

        int exit = cmd.execute("does-not-exist");

        assertNotEquals(0, exit);
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class CapturingConsole implements com.hsbc.ci.engine.core.utils.ConsoleOutput {
        String last = "";

        @Override
        public void print(String message) { last = message; }

        @Override
        public void printError(String message) { last = message; }

        @Override
        public void printf(String format, Object... args) { last = String.format(format, args); }

        @Override
        public void printfError(String format, Object... args) { last = String.format(format, args); }
    }
}
