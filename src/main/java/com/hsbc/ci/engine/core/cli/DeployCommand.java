package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(name = "deploy", description = "Manage deployments")
public class DeployCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DeployCommand.class);

    @Override
    public void run() {
        log.info("Deploy command not yet implemented");
        System.out.println("Deploy command not yet implemented");
    }
}
