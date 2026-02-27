package com.hsbc.ci.engine.core.plugin.gates;

import com.hsbc.ci.engine.core.plugin.GatePlugin;
import com.hsbc.ci.engine.core.plugin.GateResult;

import java.util.Map;
import java.util.Scanner;

public class ManualApprovalGate implements GatePlugin {

    @Override
    public String getType() {
        return "manual-approval";
    }

    @Override
    public GateResult evaluate(Map<String, Object> config, Map<String, Object> context) {
        String approver = (String) config.getOrDefault("approver", "UNKNOWN");
        String environment = (String) context.getOrDefault("environment", "unknown");
        int timeoutMinutes = (int) config.getOrDefault("timeoutMinutes", 60);
        
        System.out.println("\n========================================");
        System.out.println("Manual Approval Required");
        System.out.println("========================================");
        System.out.println("Environment: " + environment);
        System.out.println("Approver: " + approver);
        System.out.println("Timeout: " + timeoutMinutes + " minutes");
        System.out.println("========================================");
        System.out.print("Approve promotion? (yes/no): ");
        
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();
        
        if ("yes".equals(response) || "y".equals(response)) {
            return GateResult.pass("Manual approval granted by " + approver);
        } else {
            return GateResult.fail("Manual approval denied");
        }
    }
}
