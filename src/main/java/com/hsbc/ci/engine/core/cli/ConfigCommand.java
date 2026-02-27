package com.hsbc.ci.engine.core.cli;

import picocli.CommandLine.Command;

@Command(name = "config", description = "Manage configuration")
public class ConfigCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Config command not yet implemented");
    }
}
