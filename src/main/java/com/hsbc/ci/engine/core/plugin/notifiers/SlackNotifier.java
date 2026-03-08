package com.hsbc.ci.engine.core.plugin.notifiers;

import com.hsbc.ci.engine.core.plugin.NotifierPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

// TODO: not yet integrate
public class SlackNotifier implements NotifierPlugin {

    private static final Logger log = LoggerFactory.getLogger(SlackNotifier.class);
    
    private String webhookUrl;
    private String channel;

    @Override
    public String getType() {
        return "slack";
    }

    @Override
    public void init(Map<String, Object> config) {
        this.webhookUrl = (String) config.get("webhookUrl");
        this.channel = (String) config.get("channel");
    }

    @Override
    public void notify(Notification notification) {
        String message = buildSlackMessage(notification);
        
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("Slack webhook URL not configured - skipping notification");
            return;
        }
        
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                log.info("Slack notification sent successfully: {}", notification.getTitle());
            } else {
                log.error("Slack notification failed with status: {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to send Slack notification: {}", e.getMessage());
        }
    }

    private String buildSlackMessage(Notification notification) {
        String color = switch (notification.getLevel().toUpperCase()) {
            case "ERROR" -> "#ff0000";
            case "WARN" -> "#ffa500";
            default -> "#36a64f";
        };
        
        String text = String.format("{\"attachments\": [{\"color\": \"%s\", \"title\": \"%s\", \"text\": \"%s\", \"footer\": \"CI Engine\"}]}",
            color,
            escapeJson(notification.getTitle()),
            escapeJson(notification.getMessage()));
        
        return text;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    @Override
    public String getName() {
        return "slack-notifier";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public com.hsbc.ci.engine.core.plugin.PluginResult execute(Map<String, Object> context) {
        return com.hsbc.ci.engine.core.plugin.PluginResult.builder()
            .success(true)
            .message("Slack notification sent")
            .build();
    }
}
