package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(name = "stage", description = "Manage stages")
public class StageCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(StageCommand.class);

    @Override
    public void run() {
        log.info("Stage command not yet implemented");
        System.out.println("Stage command not yet implemented");
    }
}
