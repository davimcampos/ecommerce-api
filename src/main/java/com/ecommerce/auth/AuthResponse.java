package com.ecommerce.auth;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}
