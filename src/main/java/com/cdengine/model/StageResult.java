package com.cdengine.model;

import java.util.Map;

public class StageResult {
    private final String stageName;
    private final boolean success;
    private final String output;
    private final long durationMs;
    private final Map<String, String> metadata;

    private StageResult(Builder builder) {
        this.stageName = builder.stageName;
        this.success = builder.success;
        this.output = builder.output;
        this.durationMs = builder.durationMs;
        this.metadata = builder.metadata;
    }

    public String getStageName() { return stageName; }
    public boolean isSuccess() { return success; }
    public String getOutput() { return output; }
    public long getDurationMs() { return durationMs; }
    public Map<String, String> getMetadata() { return metadata; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String stageName;
        private boolean success;
        private String output;
        private long durationMs;
        private Map<String, String> metadata;

        public Builder stageName(String stageName) { this.stageName = stageName; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder output(String output) { this.output = output; return this; }
        public Builder durationMs(long durationMs) { this.durationMs = durationMs; return this; }
        public Builder metadata(Map<String, String> metadata) { this.metadata = metadata; return this; }

        public StageResult build() {
            return new StageResult(this);
        }
    }
}
