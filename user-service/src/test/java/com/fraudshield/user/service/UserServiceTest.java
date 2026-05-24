package com.fraudshield.user.service;

import com.fraudshield.user.dto.AuthResponse;
import com.fraudshield.user.dto.LoginRequest;
import com.fraudshield.user.dto.RegisterRequest;
import com.fraudshield.user.exception.BadRequestException;
import com.fraudshield.user.exception.ResourceNotFoundException;
import com.fraudshield.user.model.Role;
import com.fraudshield.user.model.User;
import com.fraudshield.user.repository.UserRepository;
import com.fraudshield.user.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("User Service Tests")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                userService, "jwtExpiration", 86400000L);

        registerRequest = RegisterRequest.builder()
                .firstName("Saurav")
                .lastName("Kumar")
                .email("saurav@gmail.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("saurav@gmail.com")
                .password("password123")
                .build();

        user = User.builder()
                .firstName("Saurav")
                .lastName("Kumar")
                .email("saurav@gmail.com")
                .password("encoded_password")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded_password");
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(jwtService.generateToken(any(User.class)))
                .thenReturn("jwt_token_here");

        AuthResponse response = userService
                .register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken())
                .isEqualTo("jwt_token_here");
        assertThat(response.getEmail())
                .isEqualTo("saurav@gmail.com");
        assertThat(response.getRole())
                .isEqualTo("USER");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email exists")
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                userService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("saurav@gmail.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class)))
                .thenReturn("jwt_token_here");

        AuthResponse response = userService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken())
                .isEqualTo("jwt_token_here");
        assertThat(response.getEmail())
                .isEqualTo("saurav@gmail.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("saurav@gmail.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

}
