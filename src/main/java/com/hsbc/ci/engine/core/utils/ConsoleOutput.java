package com.hsbc.ci.engine.core.cli;

public interface ConsoleOutput {
    void print(String message);
    void printError(String message);
    void printf(String format, Object... args);
    void printfError(String format, Object... args);
}
