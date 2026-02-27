package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(name = "config", description = "Manage configuration")
public class ConfigCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConfigCommand.class);

    @Override
    public void run() {
        log.info("Config command not yet implemented");
        System.out.println("Config command not yet implemented");
    }
}
