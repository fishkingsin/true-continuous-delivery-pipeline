package com.cdengine.cli;

import picocli.CommandLine.Command;

@Command(name = "stage", description = "Manage stages")
public class StageCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Stage command not yet implemented");
    }
}
