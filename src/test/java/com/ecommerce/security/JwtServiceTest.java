package com.ecommerce.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.config.AppProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "Em0vcSOCe0Y8ideWTHpQOtMovvKWDE+J7KTXOkcyEwY=";
    private static final String OTHER_SECRET = "09VsGxdvxiSSt+LgdLs03Xg/2nnI8d3uyg56qmJ0Sgc=";

    private JwtService service(String secret, long expirationMs) {
        return new JwtService(new AppProperties(new AppProperties.Jwt(secret, expirationMs), null));
    }

    @Test
    void shouldGenerateTokenAndExtractSubject() {
        JwtService service = service(SECRET, 60_000);

        String token = service.generateToken("user@email.com", "USER");

        assertEquals("user@email.com", service.extractSubject(token));
    }

    @Test
    void shouldExposeExpirationInSeconds() {
        assertEquals(60, service(SECRET, 60_000).getExpirationSeconds());
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService service = service(SECRET, -1_000);
        String token = service.generateToken("user@email.com", "USER");

        assertThrows(JwtException.class, () -> service.extractSubject(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        JwtService service = service(SECRET, 60_000);
        String token = service.generateToken("user@email.com", "USER");

        assertThrows(JwtException.class, () -> service.extractSubject(token + "x"));
    }

    @Test
    void shouldRejectTokenSignedWithAnotherKey() {
        String token = service(OTHER_SECRET, 60_000).generateToken("user@email.com", "USER");

        assertThrows(JwtException.class, () -> service(SECRET, 60_000).extractSubject(token));
    }
}
