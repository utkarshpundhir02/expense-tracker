package com.expensetracker.api.controller;

import com.expensetracker.api.controller.LoginController.LoginRequest;
import com.expensetracker.api.controller.LoginController.LoginResponse;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.User;
import com.expensetracker.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class LoginControllerTest {

    @InjectMocks
    private LoginController loginController;

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
    }

    @Test
    void login_success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.email = "test@example.com";
        request.password = "raw_password";

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw_password", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com")).thenReturn("mocked-jwt-token");

        // Act
        ResponseEntity<?> response = loginController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse loginResponse = (LoginResponse) response.getBody();
        assertEquals("mocked-jwt-token", loginResponse.token);
    }

    @Test
    void login_failure_invalidEmail() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.email = "invalid@example.com";
        request.password = "password";

        when(userService.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = loginController.login(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void login_failure_wrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.email = "test@example.com";
        request.password = "wrong_password";

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        // Act
        ResponseEntity<?> response = loginController.login(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }
}
