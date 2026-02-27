package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;

@Command(name = "version", description = "Print version")
public class VersionCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(VersionCommand.class);

    @Override
    public void run() {
        System.out.println("1.0.0");
        log.debug("Version command executed");
    }
}
