package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(name = "promote", description = "Promote releases")
public class PromoteCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PromoteCommand.class);

    @Override
    public void run() {
        log.info("Promote command not yet implemented");
        System.out.println("Promote command not yet implemented");
    }
}
