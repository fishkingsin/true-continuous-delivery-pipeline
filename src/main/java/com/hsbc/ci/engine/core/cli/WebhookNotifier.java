package com.hsbc.ci.engine.core.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class WebhookNotifier {

    private static final Logger log = LoggerFactory.getLogger(WebhookNotifier.class);
    
    private final String url;
    private final String method;
    private final Map<String, String> headers;

    public WebhookNotifier(String url) {
        this(url, "POST", Map.of());
    }

    public WebhookNotifier(String url, String method, Map<String, String> headers) {
        this.url = url;
        this.method = method;
        this.headers = headers;
    }

    public boolean send(Map<String, Object> payload) {
        if (url == null || url.isEmpty()) {
            log.warn("Webhook URL not configured");
            return false;
        }
        
        String json = JsonOutput.toJson(payload);
        
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");
            
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
            
            switch (method.toUpperCase()) {
                case "POST":
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(json));
                    break;
                case "PUT":
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(json));
                    break;
                case "GET":
                    requestBuilder.GET();
                    break;
                default:
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(json));
            }
            
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Webhook sent successfully: {}", url);
                return true;
            } else {
                log.error("Webhook failed with status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Webhook failed: {}", e.getMessage());
            return false;
        }
    }

    public static WebhookNotifier fromConfig(Map<String, Object> config) {
        String url = (String) config.get("url");
        String method = (String) config.getOrDefault("method", "POST");
        
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) config.get("headers");
        
        return new WebhookNotifier(url, method, headers != null ? headers : Map.of());
    }
}
