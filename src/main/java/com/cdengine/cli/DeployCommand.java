package com.cdengine.cli;

import picocli.CommandLine.Command;

@Command(name = "deploy", description = "Manage deployments")
public class DeployCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Deploy command not yet implemented");
    }
}
