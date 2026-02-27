package com.hsbc.ci.engine.core.cli;

import picocli.CommandLine.Command;

@Command(name = "version", description = "Print version")
public class VersionCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("1.0.0");
    }
}
