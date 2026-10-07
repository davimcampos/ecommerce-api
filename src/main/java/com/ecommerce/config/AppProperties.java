package com.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Admin admin) {

    public record Jwt(String secret, long expirationMs) {
    }

    public record Admin(String name, String email, String password) {
    }
}
