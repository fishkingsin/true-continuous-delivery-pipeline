package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.utils.ConsoleOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Command(name = "version", description = "Print version")
@Component
public class VersionCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(VersionCommand.class);
    private static final String VERSION = "1.0.0";

    @Autowired
    private ConsoleOutput console;

    @Override
    public void run() {
        console.print(VERSION);
        log.debug("Version command executed: {}", VERSION);
    }
}
