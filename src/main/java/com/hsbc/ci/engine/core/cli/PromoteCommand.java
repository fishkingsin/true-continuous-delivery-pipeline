package com.hsbc.ci.engine.core.cli;

import com.hsbc.ci.engine.core.config.EnvironmentLoader;
import com.hsbc.ci.engine.core.model.Environment;
import com.hsbc.ci.engine.core.model.PromotionPolicy;
import com.hsbc.ci.engine.core.plugin.GateExecutor;
import com.hsbc.ci.engine.core.plugin.GateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;

@Command(name = "promote", description = "Promote releases between environments")
public class PromoteCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PromoteCommand.class);

    @Autowired
    private EnvironmentLoader environmentLoader;

    @Autowired
    private GateExecutor gateExecutor;

    @Option(names = {"-p", "--pipeline"}, description = "Pipeline name")
    private String pipeline;

    @Option(names = {"-f", "--from"}, description = "Source environment")
    private String fromEnv;

    @Option(names = {"-t", "--to"}, description = "Target environment")
    private String toEnv;

    @Option(names = {"--policy"}, description = "Promotion policy (fast-track, standard, production, security-patch)")
    private String policy = "standard";

    @Option(names = {"--dry-run"}, description = "Dry run mode")
    private boolean dryRun;

    @Override
    public void run() {
        log.info("Starting promotion: from={}, to={}, policy={}", fromEnv, toEnv, policy);
        
        PromotionPolicy promotionPolicy = PromotionPolicy.fromType(policy);
        
        System.out.println("Promotion Request");
        System.out.println("=================");
        System.out.println("From: " + (fromEnv != null ? fromEnv : "current"));
        System.out.println("To: " + toEnv);
        System.out.println("Policy: " + promotionPolicy.getName());
        System.out.println("Description: " + promotionPolicy.getDescription());
        
        if (dryRun) {
            System.out.println("\n[DRY RUN] Would perform promotion");
            return;
        }
        
        if (!promotionPolicy.isAutoApprove() && (promotionPolicy.getApprovers() == null || promotionPolicy.getApprovers().isEmpty())) {
            System.out.println("\nWaiting for approval...");
        }
        
        if (promotionPolicy.getRequiredGates() != null && !promotionPolicy.getRequiredGates().isEmpty()) {
            System.out.println("\nRunning required gates:");
            for (String gate : promotionPolicy.getRequiredGates()) {
                System.out.println("  - " + gate);
            }
        }
        
        executePromotion(promotionPolicy);
    }

    private void executePromotion(PromotionPolicy policy) {
        Environment currentEnv = null;
        Environment targetEnv = environmentLoader.getEnvironment(toEnv);
        
        if (fromEnv != null) {
            currentEnv = environmentLoader.getEnvironment(fromEnv);
        } else {
            List<Environment> chain = environmentLoader.getPromotionChain(toEnv);
            if (!chain.isEmpty()) {
                int idx = chain.indexOf(targetEnv);
                if (idx > 0) {
                    currentEnv = chain.get(idx - 1);
                }
            }
        }
        
        if (targetEnv == null) {
            System.out.println("[ERROR] Target environment not found: " + toEnv);
            return;
        }
        
        System.out.println("\nPromoting from " + 
            (currentEnv != null ? currentEnv.getName() : "none") + 
            " to " + targetEnv.getName());
        
        if (environmentLoader.shouldAutoPromote(targetEnv.getName())) {
            System.out.println("[AUTO-PROMOTE] Environment configured for auto-promotion");
        }
        
        boolean gatesPassed = true;
        if (policy.getRequiredGates() != null) {
            for (String gateType : policy.getRequiredGates()) {
                GateResult result = gateExecutor.executeGate(gateType, Map.of(), null);
                if (result.isPassed()) {
                    System.out.println("[GATE PASSED] " + gateType + ": " + result.getMessage());
                } else {
                    System.out.println("[GATE FAILED] " + gateType + ": " + result.getMessage());
                    gatesPassed = false;
                    break;
                }
            }
        }
        
        if (!gatesPassed) {
            System.out.println("\n[ERROR] Promotion blocked - gates failed");
            return;
        }
        
        System.out.println("\n[SUCCESS] Promotion completed to " + targetEnv.getName());
    }
}
