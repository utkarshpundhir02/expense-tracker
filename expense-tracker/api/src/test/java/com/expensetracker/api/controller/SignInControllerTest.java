package com.expensetracker.api.controller;

import com.expensetracker.api.controller.SignInController.RegisterRequest;
import com.expensetracker.api.dto.UserResponseDTO;
import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.CategoryType;
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
class SignInControllerTest {

    @InjectMocks
    private SignInController signInController;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider; // Not directly used here but kept for completeness

    private RegisterRequest request;
    private User testUser;

    @BeforeEach
    void setup() {
        request = new RegisterRequest();
        request.name = "Utkarsh";
        request.email = "utkarsh@example.com";
        request.password = "test123";

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Utkarsh");
        testUser.setEmail("utkarsh@example.com");
        testUser.setPasswordHash("hashed123");
    }

    @Test
    void registerUser_success() {
        // Arrange
        when(userService.findByEmail(request.email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password)).thenReturn("hashed123");
        when(userService.registerUser(request.name, request.email, "hashed123")).thenReturn(testUser);
        when(categoryService.addCategory(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<?> response = signInController.registerUser(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof UserResponseDTO);

        UserResponseDTO dto = (UserResponseDTO) response.getBody();
        assertEquals("Utkarsh", dto.getName());
        assertEquals("utkarsh@example.com", dto.getEmail());

        verify(userService).registerUser(request.name, request.email, "hashed123");
        verify(categoryService, times(10)).addCategory(any(Category.class)); // 10 global categories
    }

    @Test
    void registerUser_emailAlreadyExists() {
        // Arrange
        when(userService.findByEmail(request.email)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = signInController.registerUser(request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already exists", response.getBody());

        verify(userService, never()).registerUser(any(), any(), any());
        verify(categoryService, never()).addCategory(any());
    }

    @Test
    void registerUser_illegalArgument() {
        // Arrange
        when(userService.findByEmail(request.email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password)).thenReturn("hashed123");
        when(userService.registerUser(request.name, request.email, "hashed123"))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        // Act
        ResponseEntity<?> response = signInController.registerUser(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data", response.getBody());

        verify(userService).registerUser(request.name, request.email, "hashed123");
        verify(categoryService, never()).addCategory(any());
    }
}
