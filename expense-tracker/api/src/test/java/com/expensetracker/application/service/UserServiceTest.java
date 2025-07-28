package com.expensetracker.application.service;

import com.expensetracker.domain.model.User;
import com.expensetracker.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPasswordHash("hashedpassword123");
    }

    @Test
    void testRegisterUser_success() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Action
        User registeredUser = userService.registerUser(
                testUser.getName(), testUser.getEmail(), testUser.getPasswordHash());

        // Assertions
        assertNotNull(registeredUser);
        assertEquals("john@example.com", registeredUser.getEmail());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_emailAlreadyExists() {
        // Setup
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Action & Assertion
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("Another User", testUser.getEmail(), "anotherPassword");
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testFindByEmail_userFound() {
        // Setup
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // Action
        Optional<User> result = userService.findByEmail("john@example.com");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findByEmail("john@example.com");
    }

    @Test
    void testFindByEmail_userNotFound() {
        // Setup
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Action
        Optional<User> result = userService.findByEmail("unknown@example.com");

        // Assertions
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("unknown@example.com");
    }
}
