package com.hsbc.ci.engine.core.cli.build;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@CommandLine.Command(name = "build", 
                     description = "Build operations",
                     subcommands = {MavenBuildCommand.class})
@Component
public class BuildCommand implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
    }
}
