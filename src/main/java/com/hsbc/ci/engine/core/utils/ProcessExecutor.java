package com.hsbc.ci.engine.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

@Component
public class ProcessExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProcessExecutor.class);

    public int execute(List<String> command, String workingDir) throws Exception {
        ProcessBuilder pb = createProcessBuilder(command, workingDir, false);
        Process process = pb.start();
        
        consumeOutput(process);
        return process.waitFor();
    }

    public int execute(List<String> command, String workingDir, boolean inheritIO) throws Exception {
        ProcessBuilder pb = createProcessBuilder(command, workingDir, inheritIO);
        Process process = pb.start();
        return process.waitFor();
    }

    public String executeAndCapture(List<String> command, String workingDir) throws Exception {
        ProcessBuilder pb = createProcessBuilder(command, workingDir, false);
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        process.waitFor();
        return output.toString();
    }

    private ProcessBuilder createProcessBuilder(List<String> command, String workingDir, boolean inheritIO) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(workingDir));
        
        if (inheritIO) {
            pb.inheritIO();
        } else {
            pb.redirectErrorStream(true);
        }
        
        log.debug("Executing: {}", String.join(" ", command));
        return pb;
    }

    private void consumeOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("  {}", line);
            }
        }
    }
}
