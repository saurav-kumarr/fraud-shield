package com.fraudshield.user.service;

import com.fraudshield.user.model.Role;
import com.fraudshield.user.model.User;
import com.fraudshield.user.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Service Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String SECRET_KEY =
            "ZnJhdWRTaGllbGRTZWNyZXRLZXkyMDI2U2F1cmF2S3VtYXJGcmF1ZERldGVjdGlvbg==";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(
                jwtService, "jwtExpiration", 86400000L);

        userDetails = User.builder()
                .firstName("Saurav")
                .lastName("Kumar")
                .email("saurav@gmail.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should generate JWT token")
    void shouldGenerateJwtToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("saurav@gmail.com");
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService
                .isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        UserDetails anotherUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@gmail.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();

        String token1 = jwtService.generateToken(userDetails);
        String token2 = jwtService.generateToken(anotherUser);

        assertThat(token1).isNotEqualTo(token2);
    }
}
