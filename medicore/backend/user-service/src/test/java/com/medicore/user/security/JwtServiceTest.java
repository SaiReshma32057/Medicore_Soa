package com.medicore.user.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "test-secret-key-that-is-at-least-32-chars";

    @Test
    void generatedTokenContainsSubjectAndRole() {
        JwtService service = new JwtService(SECRET, 3_600_000);
        String token = service.generate("user@example.com", "USER");

        assertEquals("user@example.com", service.username(token));
        assertEquals("USER", service.role(token));
        assertTrue(service.valid(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(SECRET, 3_600_000);
        String token = service.generate("user@example.com", "USER");

        assertFalse(service.valid(token + "tampered"));
    }
}
