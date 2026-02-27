package com.hsbc.ci.engine.core.model;

import java.util.Map;

public record StageResult(
    String stageName,
    boolean success,
    String output,
    long durationMs,
    Map<String, String> metadata
) {
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
            return new StageResult(
                stageName,
                success,
                output,
                durationMs,
                metadata
            );
        }
    }
}
