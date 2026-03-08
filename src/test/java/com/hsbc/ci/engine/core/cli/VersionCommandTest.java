package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.utils.ConsoleOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionCommandTest {

    @Test
    void printsVersion() {
        CapturingConsole console = new CapturingConsole();
        VersionCommand command = new VersionCommand();
        inject(command, "console", console);

        command.run();

        assertTrue(console.lastLine().contains("1.0.0"));
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            var field = VersionCommand.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class CapturingConsole implements ConsoleOutput {
        private String last = "";

        @Override
        public void print(String message) { last = message; }

        @Override
        public void printError(String message) { last = message; }

        @Override
        public void printf(String format, Object... args) { last = String.format(format, args); }

        @Override
        public void printfError(String format, Object... args) { last = String.format(format, args); }

        String lastLine() { return last; }
    }
}
