package com.raul.forumhub.authorization.server.security.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig implements CorsConfigurationSource {

    private final RegisteredClientProperties clientProperties;

    public CorsConfig(RegisteredClientProperties clientProperties) {
        this.clientProperties = clientProperties;
    }

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of(clientProperties.topicUrl, clientProperties.userUrl));
        corsConfiguration.setAllowedMethods(List.of("OPTIONS", "GET", "POST"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(0L);
        return corsConfiguration;
    }
}
