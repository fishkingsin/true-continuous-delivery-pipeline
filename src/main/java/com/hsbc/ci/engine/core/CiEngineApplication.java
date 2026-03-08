package com.hsbc.ci.engine.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hsbc.ci.engine.core.config.HttpClientProperties;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@Configuration
@EnableConfigurationProperties(HttpClientProperties.class)
public class CiEngineApplication {
    
    private static final Logger log = LoggerFactory.getLogger(CiEngineApplication.class);

    public static void main(String[] args) {
        printBanner();
        SpringApplication.run(CiEngineApplication.class, args);
    }

    @Bean
    public CommandLineRunner checkPrerequisites() {
        return args -> {
            checkDirectory("plugins", "External plugins (optional)");
            checkDirectory("config/pipelines", "Pipeline definitions");
            checkDirectory("config/environments", "Environment configs");
        };
    }

    private void checkDirectory(String path, String description) {
        Path dir = Path.of(path);
        if (!Files.exists(dir)) {
            log.warn("Directory missing: {} ({})", path, description);
            try {
                Files.createDirectories(dir);
                log.info("Created directory: {}", path);
            } catch (Exception e) {
                log.warn("Could not create {}: {}", path, e.getMessage());
            }
        }
    }

    private static void printBanner() {
        String banner = """
            
            ╔═══════════════════════════════════════════════════════════════╗
            ║                    CD Engine Core CLI v1.0.0                  ║
            ╠═══════════════════════════════════════════════════════════════╣
            ║  Prerequisites:                                               ║
            ║    - Java 21+                                                 ║
            ║    - Maven 3.8+ (for Maven builds)                            ║
            ║    - Docker (for containerize stage - optional)               ║
            ║    - kubectl (for K8s deployments - optional)                 ║
            ║    - AWS CLI (for ECS deployments - optional)                 ║
            ║                                                               ║
            ║  Directories:                                                 ║
            ║    plugins/            - External plugin JARs (optional)      ║
            ║    config/pipelines/   - Pipeline YAML definitions            ║
            ║    config/environments/- Environment configurations           ║
            ╚═══════════════════════════════════════════════════════════════╝
            """;
        System.out.println(banner);
    }
}
