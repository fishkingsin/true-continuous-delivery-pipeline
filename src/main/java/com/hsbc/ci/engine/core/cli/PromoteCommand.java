package com.hsbc.ci.engine.core.cli;

import picocli.CommandLine.Command;

@Command(name = "promote", description = "Promote releases")
public class PromoteCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Promote command not yet implemented");
    }
}
