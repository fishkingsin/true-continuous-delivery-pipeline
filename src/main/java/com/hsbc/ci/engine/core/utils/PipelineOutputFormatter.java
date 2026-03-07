package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.model.StageResult;
import java.util.Map;

/**
 * Formats pipeline execution results for console output.
 * Stateless formatter with static methods for output formatting.
 */
public class PipelineOutputFormatter {

    private static final int DEFAULT_WIDTH = 78;
    
    private PipelineOutputFormatter() {
        // Utility class
    }

    public static String formatPipelineOutput(String pipelineName, String environment, 
                                      Map<String, StageResult> stageResults, boolean failed) {
        StringBuilder output = new StringBuilder();
        
        output.append("[INFO] Loading pipeline: ").append(pipelineName).append("\n");
        output.append("[INFO] Environment: ").append(environment != null ? environment : "default").append("\n");
        output.append("\n");
        
        output.append(formatBox(pipelineName, stageResults, failed));
        
        return output.toString();
    }

    private static String formatBox(String pipelineName, Map<String, StageResult> stageResults, boolean failed) {
        StringBuilder box = new StringBuilder();
        int width = DEFAULT_WIDTH;
        String title = "CD Pipeline: " + pipelineName;
        
        // Top border
        box.append(drawBorder(width, "╔", "═", "╗")).append("\n");
        
        // Title
        box.append(formatLine(title, width)).append("\n");
        
        // Middle separator
        box.append(drawBorder(width, "╠", "═", "╣")).append("\n");
        
        // Stage results
        if (stageResults != null && !stageResults.isEmpty()) {
            for (Map.Entry<String, StageResult> entry : stageResults.entrySet()) {
                box.append(formatStageResult(entry.getKey(), entry.getValue(), width)).append("\n");
            }
        } else {
            box.append(formatLine("(No stages executed)", width)).append("\n");
        }
        
        // Status separator
        box.append(drawBorder(width, "╠", "═", "╣")).append("\n");
        
        // Status footer
        String status = failed 
            ? "[FAILED] Pipeline execution failed" 
            : "[SUCCESS] Pipeline completed successfully";
        box.append(formatStatusLine(status, width)).append("\n");
        
        // Bottom border
        box.append(drawBorder(width, "╚", "═", "╝"));
        
        return box.toString();
    }

    private static String drawBorder(int width, String left, String middle, String right) {
        return left + middle.repeat(width - 2) + right;
    }

    private static String formatLine(String text, int width) {
        return String.format("║  %-" + (width - 4) + "s║", text);
    }

    private static String formatStatusLine(String status, int width) {
        return String.format("║  %-" + (width - 4) + "s║", status);
    }

    private static String formatStageResult(String stageName, StageResult result, int width) {
        String statusIcon = result.isSuccess() ? "✓" : "✗";
        String progress = result.isSuccess() ? "100%" : "0%";
        String progressBar = result.isSuccess() ? "████████████" : "............";
        
        // Calculate max length for stage name (width - icon - brackets - percentage - borders)
        int maxNameLength = width - 30;
        String displayName = stageName;
        if (stageName.length() > maxNameLength) {
            displayName = stageName.substring(0, maxNameLength - 3) + "...";
        }
        
        return String.format("║  %s %-" + maxNameLength + "s [%s] %5s   ║", 
            statusIcon, displayName, progressBar, progress);
    }
}
