package com.hsbc.ci.engine.core.model;

import java.util.List;

public class PromotionPolicy {
    private String name;
    private String description;
    private String type;
    private List<String> requiredGates;
    private List<String> approvers;
    private boolean autoApprove;
    private int timeoutMinutes;
    private boolean allowRollback;

    public PromotionPolicy() {
    }

    public PromotionPolicy(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getRequiredGates() { return requiredGates; }
    public void setRequiredGates(List<String> requiredGates) { this.requiredGates = requiredGates; }

    public List<String> getApprovers() { return approvers; }
    public void setApprovers(List<String> approvers) { this.approvers = approvers; }

    public boolean isAutoApprove() { return autoApprove; }
    public void setAutoApprove(boolean autoApprove) { this.autoApprove = autoApprove; }

    public int getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }

    public boolean isAllowRollback() { return allowRollback; }
    public void setAllowRollback(boolean allowRollback) { this.allowRollback = allowRollback; }

    public static PromotionPolicy fastTrack() {
        PromotionPolicy policy = new PromotionPolicy("fast-track", "FAST_TRACK");
        policy.setDescription("Fast-track promotion - bypasses most approvals");
        policy.setAutoApprove(true);
        policy.setTimeoutMinutes(5);
        policy.setAllowRollback(true);
        return policy;
    }

    public static PromotionPolicy standard() {
        PromotionPolicy policy = new PromotionPolicy("standard", "STANDARD");
        policy.setDescription("Standard promotion with approval");
        policy.setAutoApprove(false);
        policy.setRequiredGates(List.of("test-passed"));
        policy.setTimeoutMinutes(60);
        policy.setAllowRollback(true);
        return policy;
    }

    public static PromotionPolicy production() {
        PromotionPolicy policy = new PromotionPolicy("production", "PRODUCTION");
        policy.setDescription("Production promotion - requires all gates and approval");
        policy.setAutoApprove(false);
        policy.setRequiredGates(List.of("test-passed", "coverage-threshold", "security-scan"));
        policy.setTimeoutMinutes(120);
        policy.setAllowRollback(true);
        return policy;
    }

    public static PromotionPolicy securityPatch() {
        PromotionPolicy policy = new PromotionPolicy("security-patch", "SECURITY_PATCH");
        policy.setDescription("Security patch - expedited approval");
        policy.setAutoApprove(false);
        policy.setRequiredGates(List.of("test-passed", "security-scan"));
        policy.setTimeoutMinutes(30);
        policy.setAllowRollback(true);
        return policy;
    }

    public static PromotionPolicy fromType(String type) {
        return switch (type.toLowerCase()) {
            case "fast-track", "fasttrack" -> fastTrack();
            case "standard" -> standard();
            case "production" -> production();
            case "security-patch", "securitypatch" -> securityPatch();
            default -> new PromotionPolicy(type, type.toUpperCase());
        };
    }
}
