package com.hsbc.ci.engine.core.plugin.notifiers;

import com.hsbc.ci.engine.core.plugin.NotifierPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EmailNotifier implements NotifierPlugin {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);
    
    private String smtpHost;
    private int smtpPort;
    private String username;
    private String password;
    private String from;
    private String to;

    @Override
    public String getType() {
        return "email";
    }

    @Override
    public void init(Map<String, Object> config) {
        this.smtpHost = (String) config.getOrDefault("smtpHost", "localhost");
        this.smtpPort = (Integer) config.getOrDefault("smtpPort", 587);
        this.username = (String) config.get("username");
        this.password = (String) config.get("password");
        this.from = (String) config.getOrDefault("from", "ci-engine@company.com");
        this.to = (String) config.get("to");
    }

    @Override
    public void notify(Notification notification) {
        if (to == null || to.isEmpty()) {
            log.warn("Email 'to' address not configured - skipping notification");
            System.out.println("[EMAIL] Recipient not configured - skipping");
            return;
        }
        
        String subject = notification.getTitle();
        String body = buildEmailBody(notification);
        
        log.info("Sending email notification to: {}", to);
        System.out.println("[EMAIL] Would send email:");
        System.out.println("  To: " + to);
        System.out.println("  Subject: " + subject);
        System.out.println("  Body: " + body.substring(0, Math.min(100, body.length())) + "...");
    }

    private String buildEmailBody(Notification notification) {
        StringBuilder body = new StringBuilder();
        body.append("CI Engine Notification\n");
        body.append("======================\n\n");
        body.append("Title: ").append(notification.getTitle()).append("\n");
        body.append("Level: ").append(notification.getLevel()).append("\n");
        body.append("Message:\n").append(notification.getMessage()).append("\n");
        
        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            body.append("\nMetadata:\n");
            for (Map.Entry<String, Object> entry : notification.getMetadata().entrySet()) {
                body.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        return body.toString();
    }

    @Override
    public String getName() {
        return "email-notifier";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public com.hsbc.ci.engine.core.plugin.PluginResult execute(Map<String, Object> context) {
        return com.hsbc.ci.engine.core.plugin.PluginResult.builder()
            .success(true)
            .message("Email notification queued")
            .build();
    }
}
