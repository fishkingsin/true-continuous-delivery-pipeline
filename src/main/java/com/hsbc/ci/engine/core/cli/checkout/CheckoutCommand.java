package com.hsbc.ci.engine.core.cli.checkout;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@CommandLine.Command(name = "checkout", 
                     description = "Git checkout operations",
                     subcommands = {CloneCommand.class})
@Component
public class CheckoutCommand implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
    }
}
