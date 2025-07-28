package com.expensetracker.application.service;

import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.CategoryType;
import com.expensetracker.domain.model.User;
import com.expensetracker.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private User user;

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
    }

    @Test
    void testAddCategory() {
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = categoryService.addCategory(category);

        assertNotNull(result);
        assertEquals("Food", result.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void testGetCategoriesByUser() {
        when(categoryRepository.findByUser(user)).thenReturn(List.of(category));

        List<Category> categories = categoryService.getCategoriesByUser(user);

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Food", categories.get(0).getName());
        verify(categoryRepository, times(1)).findByUser(user);
    }

    @Test
    void testGetCategoryById_found() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));

        Optional<Category> result = categoryService.getCategoryById(100L);

        assertTrue(result.isPresent());
        assertEquals("Food", result.get().getName());
        verify(categoryRepository, times(1)).findById(100L);
    }

    @Test
    void testGetCategoryById_notFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Category> result = categoryService.getCategoryById(999L);

        assertFalse(result.isPresent());
        verify(categoryRepository, times(1)).findById(999L);
    }

    @Test
    void testFindByNameAndTypeAndUser() {
        when(categoryRepository.findByNameAndTypeAndUser("Food", CategoryType.EXPENSE, user))
                .thenReturn(Optional.of(category));

        Optional<Category> result = categoryService.findByNameAndTypeAndUser("Food", CategoryType.EXPENSE, user);

        assertTrue(result.isPresent());
        assertEquals(category, result.get());
        verify(categoryRepository, times(1))
                .findByNameAndTypeAndUser("Food", CategoryType.EXPENSE, user);
    }

    @Test
    void testGetCategoriesByTypeForUser() {
        when(categoryRepository.findByTypeAndUser(CategoryType.EXPENSE, user)).thenReturn(List.of(category));

        List<Category> result = categoryService.getCategoriesByTypeForUser("expense", user);

        assertEquals(1, result.size());
        assertEquals(CategoryType.EXPENSE, result.get(0).getType());
        verify(categoryRepository, times(1)).findByTypeAndUser(CategoryType.EXPENSE, user);
    }

    @Test
    void testUpdateCategory() {
        category.setName("Groceries");

        when(categoryRepository.save(category)).thenReturn(category);

        Category updated = categoryService.updateCategory(category);

        assertNotNull(updated);
        assertEquals("Groceries", updated.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void testDeleteCategory() {
        doNothing().when(categoryRepository).deleteById(100L);

        categoryService.deleteCategory(100L);

        verify(categoryRepository, times(1)).deleteById(100L);
    }
}
