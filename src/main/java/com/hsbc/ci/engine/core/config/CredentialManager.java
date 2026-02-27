package com.hsbc.ci.engine.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CredentialManager {

    private static final Logger log = LoggerFactory.getLogger(CredentialManager.class);
    
    private final Map<String, Credential> credentials = new ConcurrentHashMap<>();

    public void registerCredential(Credential credential) {
        credentials.put(credential.getName(), credential);
        log.info("Registered credential: {}", credential.getName());
    }

    public Credential getCredential(String name) {
        return credentials.get(name);
    }

    public boolean hasCredential(String name) {
        return credentials.containsKey(name);
    }

    public Collection<Credential> listCredentials() {
        return credentials.values();
    }

    public void loadFromFile(String path) throws Exception {
        Path credentialFile = Paths.get(path);
        if (!Files.exists(credentialFile)) {
            log.warn("Credential file not found: {}", path);
            return;
        }
        
        Properties props = new Properties();
        props.load(Files.newInputStream(credentialFile));
        
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            String[] parts = key.split("\\.", 2);
            if (parts.length == 2) {
                String type = parts[0];
                String name = parts[1];
                registerCredential(new Credential(name, type, value));
            }
        }
        
        log.info("Loaded {} credentials from {}", credentials.size(), path);
    }

    public void loadFromEnvironment() {
        String gitToken = System.getenv("CI_GIT_TOKEN");
        if (gitToken != null) {
            registerCredential(new Credential("git", "token", gitToken));
        }
        
        String dockerUser = System.getenv("CI_DOCKER_USERNAME");
        String dockerPass = System.getenv("CI_DOCKER_PASSWORD");
        if (dockerUser != null && dockerPass != null) {
            registerCredential(new Credential("docker", "credentials", dockerUser + ":" + dockerPass));
        }
        
        String awsKey = System.getenv("CI_AWS_ACCESS_KEY");
        String awsSecret = System.getenv("CI_AWS_SECRET_KEY");
        if (awsKey != null && awsSecret != null) {
            registerCredential(new Credential("aws", "credentials", awsKey + ":" + awsSecret));
        }
        
        log.info("Loaded credentials from environment");
    }

    public static class Credential {
        private final String name;
        private final String type;
        private final String value;

        public Credential(String name, String type, String value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public String getValue() { return value; }
    }
}
