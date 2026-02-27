package com.hsbc.ci.engine.core.plugin;

import java.util.HashMap;
import java.util.Map;

public class GateResult {
    private final boolean passed;
    private final String message;
    private final Map<String, Object> metrics;
    private final String severity;

    private GateResult(Builder builder) {
        this.passed = builder.passed;
        this.message = builder.message;
        this.metrics = builder.metrics;
        this.severity = builder.severity;
    }

    public boolean isPassed() { return passed; }
    public String getMessage() { return message; }
    public Map<String, Object> getMetrics() { return metrics; }
    public String getSeverity() { return severity; }

    public static Builder builder() {
        return new Builder();
    }

    public static GateResult pass(String message) {
        return builder().passed(true).message(message).severity("INFO").build();
    }

    public static GateResult fail(String message) {
        return builder().passed(false).message(message).severity("ERROR").build();
    }

    public static GateResult warn(String message) {
        return builder().passed(true).message(message).severity("WARN").build();
    }

    public static class Builder {
        private boolean passed = true;
        private String message = "";
        private Map<String, Object> metrics = new HashMap<>();
        private String severity = "INFO";

        public Builder passed(boolean passed) { this.passed = passed; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder metrics(Map<String, Object> metrics) { this.metrics = metrics; return this; }
        public Builder severity(String severity) { this.severity = severity; return this; }

        public GateResult build() {
            return new GateResult(this);
        }
    }
}
