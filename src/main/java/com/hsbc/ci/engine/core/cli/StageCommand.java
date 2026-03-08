package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.utils.ConsoleOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Command(name = "stage", description = "Manage stages")
@Component
public class StageCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(StageCommand.class);

    @Autowired
    private ConsoleOutput console;

    @Override
    public void run() {
        log.info("Stage command not yet implemented");
        console.print("Stage command not yet implemented");
    }
}
