package com.labtrack.labtrack.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "dev-only-secret-change-me-0123456789abcdef";

    private final UserDetails userDetails = User.withUsername("tecnico.teste")
            .password("irrelevant")
            .authorities(List.of())
            .build();

    @Test
    void generatesTokenAndExtractsUsername() {
        JwtService jwtService = new JwtService(SECRET, 28_800_000L);

        String token = jwtService.generateToken(userDetails);

        assertEquals("tecnico.teste", jwtService.extractUsername(token));
    }

    @Test
    void tokenIsValidForMatchingUserAndNotExpired() {
        JwtService jwtService = new JwtService(SECRET, 28_800_000L);

        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void tokenIsInvalidWhenExpired() {
        JwtService jwtService = new JwtService(SECRET, -1000L);

        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void tokenIsInvalidWithWrongSignature() {
        JwtService jwtService = new JwtService(SECRET, 28_800_000L);
        JwtService otherJwtService = new JwtService(
                "different-secret-key-thats-also-long-enough-0123456789", 28_800_000L);

        String token = jwtService.generateToken(userDetails);

        assertFalse(otherJwtService.isTokenValid(token, userDetails));
    }
}
