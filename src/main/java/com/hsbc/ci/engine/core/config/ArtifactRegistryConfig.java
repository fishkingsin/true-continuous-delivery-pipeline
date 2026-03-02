package com.hsbc.ci.engine.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "artifact-registry")
public class ArtifactRegistryConfig {

    private Map<String, DockerRegistry> dockerRegistries = new HashMap<>();
    private Map<String, S3Bucket> s3Buckets = new HashMap<>();
    private String defaultDockerRegistry;
    private String defaultS3Bucket;

    public Map<String, DockerRegistry> getDockerRegistries() {
        return dockerRegistries;
    }

    public void setDockerRegistries(Map<String, DockerRegistry> dockerRegistries) {
        this.dockerRegistries = dockerRegistries;
    }

    public Map<String, S3Bucket> getS3Buckets() {
        return s3Buckets;
    }

    public void setS3Buckets(Map<String, S3Bucket> s3Buckets) {
        this.s3Buckets = s3Buckets;
    }

    public String getDefaultDockerRegistry() {
        return defaultDockerRegistry;
    }

    public void setDefaultDockerRegistry(String defaultDockerRegistry) {
        this.defaultDockerRegistry = defaultDockerRegistry;
    }

    public String getDefaultS3Bucket() {
        return defaultS3Bucket;
    }

    public void setDefaultS3Bucket(String defaultS3Bucket) {
        this.defaultS3Bucket = defaultS3Bucket;
    }

    public DockerRegistry getDockerRegistry(String name) {
        return dockerRegistries.get(name);
    }

    public DockerRegistry getDefaultDockerRegistryConfig() {
        if (defaultDockerRegistry != null) {
            return dockerRegistries.get(defaultDockerRegistry);
        }
        return dockerRegistries.values().stream().findFirst().orElse(null);
    }

    public S3Bucket getS3Bucket(String name) {
        return s3Buckets.get(name);
    }

    public S3Bucket getDefaultS3BucketConfig() {
        if (defaultS3Bucket != null) {
            return s3Buckets.get(defaultS3Bucket);
        }
        return s3Buckets.values().stream().findFirst().orElse(null);
    }

    public static class DockerRegistry {
        private String url;
        private String username;
        private String password;
        private String namespace;
        private boolean useHttps = true;
        private Map<String, String> tags = new HashMap<>();

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public boolean isUseHttps() {
            return useHttps;
        }

        public void setUseHttps(boolean useHttps) {
            this.useHttps = useHttps;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }

        public String getRepositoryUrl(String imageName) {
            String baseUrl = useHttps ? "https://" : "http://";
            return baseUrl + url + "/" + namespace + "/" + imageName;
        }
    }

    public static class S3Bucket {
        private String name;
        private String region;
        private String accessKey;
        private String secretKey;
        private String prefix = "";
        private String endpoint;
        private boolean usePathStyle = false;
        private Map<String, String> tags = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public boolean isUsePathStyle() {
            return usePathStyle;
        }

        public void setUsePathStyle(boolean usePathStyle) {
            this.usePathStyle = usePathStyle;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }

        public String getObjectKey(String artifactName, String version) {
            if (prefix.isEmpty()) {
                return artifactName + "/" + version;
            }
            return prefix + "/" + artifactName + "/" + version;
        }
    }
}
