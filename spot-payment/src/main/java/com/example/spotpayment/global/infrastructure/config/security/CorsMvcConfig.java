package com.example.Spot.global.infrastructure.config.security;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConfigurationProperties(prefix = "spring.mvc.cors")
public class CorsMvcConfig implements WebMvcConfigurer {

    private Map<String, CorsPathConfig> mappings;

    public void setMappings(Map<String, CorsPathConfig> mappings) {
        this.mappings = mappings;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (mappings != null) {
            mappings.forEach((path, config) -> {
                registry.addMapping(path)
                        .allowedOriginPatterns(config.getAllowedOriginPatterns().toArray(new String[0]))
                        .allowedMethods(config.getAllowedMethods().toArray(new String[0]))
                        .allowedHeaders(config.getAllowedHeaders().toArray(new String[0]))
                        .exposedHeaders(config.getExposedHeaders().toArray(new String[0]))
                        .allowCredentials(config.isAllowCredentials());
            });
        }
    }

    public static class CorsPathConfig {
        private List<String> allowedOriginPatterns;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private List<String> exposedHeaders;
        private boolean allowCredentials;

        public List<String> getAllowedOriginPatterns() {
             return allowedOriginPatterns;
        }
        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns; 
        }
        public List<String> getAllowedMethods() {
            return allowedMethods; 
        }
        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }
        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }
        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }
        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }
        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }
        public boolean isAllowCredentials() {
            return allowCredentials;
        }
        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }
    }
}
