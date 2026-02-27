package com.hsbc.ci.engine.core.plugin;

import java.util.Map;

public interface NotifierPlugin extends Plugin {
    String getType();
    void notify(Notification notification);
    
    class Notification {
        private final String title;
        private final String message;
        private final String level;
        private final Map<String, Object> metadata;

        private Notification(Builder builder) {
            this.title = builder.title;
            this.message = builder.message;
            this.level = builder.level;
            this.metadata = builder.metadata;
        }

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getLevel() { return level; }
        public Map<String, Object> getMetadata() { return metadata; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String title = "";
            private String message = "";
            private String level = "INFO";
            private Map<String, Object> metadata;

            public Builder title(String title) { this.title = title; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder level(String level) { this.level = level; return this; }
            public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }

            public Notification build() {
                return new Notification(this);
            }
        }
    }
}
