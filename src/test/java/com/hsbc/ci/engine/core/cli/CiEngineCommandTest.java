package com.hsbc.ci.engine.core.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class CiEngineCommandTest {

    @Test
    void helpOptionPrintsUsage() {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        CommandLine cmd = new CommandLine(new CiEngineCommand.RootCommand());
        cmd.setOut(new PrintWriter(out));
        cmd.setErr(new PrintWriter(err));

        int exitCode = cmd.execute("--help");

        String output = out.toString();
        assertEquals(0, exitCode);
        assertTrue(output.contains("Enterprise CD Pipeline Engine"));
        assertTrue(output.contains("Documentation: https://docs.company.com/ci-engine"));
    }

    @Test
    void noArgsPrintsUsage() {
        CommandLine cmd = new CommandLine(new CiEngineCommand.RootCommand());

        int exitCode = cmd.execute();

        assertEquals(0, exitCode);
    }
}
