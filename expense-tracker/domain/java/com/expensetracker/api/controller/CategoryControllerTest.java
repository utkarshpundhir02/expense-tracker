package com.expensetracker.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import com.expensetracker.config.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.CategoryType;
import com.expensetracker.domain.model.User;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock private CategoryService categoryService;
    @Mock private UserService userService;
    @Mock private Authentication authentication;

    @InjectMocks
    private CategoryController categoryController;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        category = new Category();
        category.setId(100L);
        category.setName("Food");
        category.setType(CategoryType.EXPENSE);
        category.setUser(user);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void addCategory_success() {
        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest();
        request.name = "Groceries";
        request.type = "EXPENSE";

        when(categoryService.findByNameAndTypeAndUser("Groceries", CategoryType.EXPENSE, user))
                .thenReturn(Optional.empty());
        when(categoryService.addCategory(any())).thenReturn(category);

        ResponseEntity<?> response = categoryController.addCategory(request, authentication);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(category, response.getBody());
    }

    @Test
    void addCategory_duplicate_throwsException() {
        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest();
        request.name = "Food";
        request.type = "EXPENSE";

        when(categoryService.findByNameAndTypeAndUser("Food", CategoryType.EXPENSE, user))
                .thenReturn(Optional.of(category));

        assertThrows(IllegalArgumentException.class,
                () -> categoryController.addCategory(request, authentication));
    }

    @Test
    void getCategories_success() {
        when(categoryService.getCategoriesByUser(user)).thenReturn(List.of(category));

        ResponseEntity<List<Category>> response = categoryController.getCategories(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getCategoriesByType_success() {
        when(categoryService.getCategoriesByTypeForUser("EXPENSE", user)).thenReturn(List.of(category));

        ResponseEntity<List<Category>> response = categoryController.getCategoriesByType("expense", authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Food", response.getBody().get(0).getName());
    }

    @Test
    void getCategoryById_success() throws AccessDeniedException {
        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));

        ResponseEntity<?> response = categoryController.getCategoryById(category.getId(), authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(category, response.getBody());
    }

    @Test
    void getCategoryById_notFound() {
        when(categoryService.getCategoryById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryController.getCategoryById(999L, authentication));
    }

    @Test
    void getCategoryById_accessDenied() {
        User otherUser = new User();
        otherUser.setId(999L);
        category.setUser(otherUser);

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));

        assertThrows(AccessDeniedException.class,
                () -> categoryController.getCategoryById(category.getId(), authentication));
    }

    @Test
    void updateCategory_success() {
        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest();
        request.name = "Transport";
        request.type = "EXPENSE";

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));
        when(categoryService.updateCategory(any())).thenReturn(category);

        ResponseEntity<?> response = categoryController.updateCategory(category.getId(), request, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(category, response.getBody());
    }

    @Test
    void updateCategory_notFound() {
        when(categoryService.getCategoryById(999L)).thenReturn(Optional.empty());

        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest();
        request.name = "Misc";
        request.type = "INCOME";

        ResponseEntity<?> response = categoryController.updateCategory(999L, request, authentication);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Category not found", response.getBody());
    }

    @Test
    void updateCategory_accessDenied() {
        User otherUser = new User();
        otherUser.setId(888L);
        category.setUser(otherUser);

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));

        CategoryController.CategoryRequest request = new CategoryController.CategoryRequest();
        request.name = "Blocked";
        request.type = "EXPENSE";

        ResponseEntity<?> response = categoryController.updateCategory(category.getId(), request, authentication);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void deleteCategory_success() {
        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));

        ResponseEntity<?> response = categoryController.deleteCategory(category.getId(), authentication);

        assertEquals(204, response.getStatusCodeValue());
        verify(categoryService, times(1)).deleteCategory(category.getId());
    }

    @Test
    void deleteCategory_notFound() {
        when(categoryService.getCategoryById(123L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = categoryController.deleteCategory(123L, authentication);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Category not found", response.getBody());
    }

    @Test
    void deleteCategory_accessDenied() {
        User anotherUser = new User();
        anotherUser.setId(99L);
        category.setUser(anotherUser);

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));

        ResponseEntity<?> response = categoryController.deleteCategory(category.getId(), authentication);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }
}
