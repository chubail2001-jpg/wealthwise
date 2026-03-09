package com.wealthwise.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // Must be at least 32 chars for HS256
    private static final String SECRET = "dGVzdFNlY3JldEtleUZvclRlc3RpbmdPbmx5VXNlSW5UZXN0cw==";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",    SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", 86400000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtils.generateToken("alice");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtUtils.generateToken("alice");
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("alice");
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtUtils.generateToken("bob");
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalseForTamperedToken() {
        String token = jwtUtils.generateToken("bob");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertThat(jwtUtils.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_returnsFalseForEmptyString() {
        assertThat(jwtUtils.validateToken("")).isFalse();
    }

    @Test
    void generateToken_differentUsersProduceDifferentTokens() {
        String tokenA = jwtUtils.generateToken("alice");
        String tokenB = jwtUtils.generateToken("bob");
        assertThat(tokenA).isNotEqualTo(tokenB);
    }
}
