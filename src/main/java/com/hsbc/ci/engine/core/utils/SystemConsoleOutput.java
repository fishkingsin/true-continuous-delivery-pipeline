package com.hsbc.ci.engine.core.utils;

import org.springframework.stereotype.Component;

import java.io.PrintStream;

@Component
public class SystemConsoleOutput implements ConsoleOutput {

    private final PrintStream out;
    private final PrintStream err;

    public SystemConsoleOutput() {
        this(System.out, System.err);
    }

    public SystemConsoleOutput(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    @Override
    public void print(String message) {
        out.println(message);
    }

    @Override
    public void printError(String message) {
        err.println(message);
    }

    @Override
    public void printf(String format, Object... args) {
        out.printf(format, args);
    }

    @Override
    public void printfError(String format, Object... args) {
        err.printf(format, args);
    }
}
