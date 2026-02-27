package com.hsbc.ci.engine.core.plugin;

import java.util.HashMap;
import java.util.Map;

public class PluginResult {
    private final boolean success;
    private final String message;
    private final Map<String, Object> data;
    private final long durationMs;

    private PluginResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
        this.durationMs = builder.durationMs;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Map<String, Object> getData() { return data; }
    public long getDurationMs() { return durationMs; }

    public static Builder builder() {
        return new Builder();
    }

    public static PluginResult success(String message) {
        return builder().success(true).message(message).build();
    }

    public static PluginResult success(String message, Map<String, Object> data) {
        return builder().success(true).message(message).data(data).build();
    }

    public static PluginResult failure(String message) {
        return builder().success(false).message(message).build();
    }

    public static class Builder {
        private boolean success = true;
        private String message = "";
        private Map<String, Object> data = new HashMap<>();
        private long durationMs = 0;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder data(Map<String, Object> data) { this.data = data; return this; }
        public Builder durationMs(long durationMs) { this.durationMs = durationMs; return this; }

        public PluginResult build() {
            return new PluginResult(this);
        }
    }
}
