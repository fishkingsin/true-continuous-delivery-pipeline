package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.utils.ConsoleOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StageCommandTest {

    @Test
    void printsNotImplementedMessage() {
        CaptureConsole console = new CaptureConsole();
        StageCommand command = new StageCommand();
        inject(command, "console", console);

        command.run();

        assertTrue(console.last.contains("not yet implemented"));
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            var field = StageCommand.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class CaptureConsole implements ConsoleOutput {
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
