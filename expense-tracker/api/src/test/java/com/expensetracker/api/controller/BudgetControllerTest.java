package com.expensetracker.api.controller;

import com.expensetracker.application.service.CategoryBudgetService;
import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    @Mock private CategoryBudgetService budgetService;
    @Mock private UserService userService;
    @Mock private CategoryService categoryService;
    @Mock private Authentication authentication;

    @InjectMocks
    private BudgetController budgetController;

    private User user;
    private Category category;
    private Budget budget;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        category = new Category();
        category.setId(10L);
        category.setUser(user);

        budget = new Budget();
        budget.setId(100L);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(BigDecimal.valueOf(1000));
        budget.setMonth(7);
        budget.setYear(2025);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void testCreateBudget_success() {
        BudgetController.CategoryBudgetRequest request = new BudgetController.CategoryBudgetRequest();
        request.categoryId = category.getId();
        request.month = 7;
        request.year = 2025;
        request.amount = BigDecimal.valueOf(1000);

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));
        when(budgetService.getByUserCategoryMonthYear(user.getId(), category.getId(), 7, 2025))
                .thenReturn(Optional.empty());
        when(budgetService.saveOrUpdateCategoryBudget(user, category, 7, 2025, request.amount))
                .thenReturn(budget);

        ResponseEntity<?> response = budgetController.createCategoryBudget(request, authentication);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(budget, response.getBody());
    }

    @Test
    void testCreateBudget_categoryNotOwnedByUser_forbidden() {
        User anotherUser = new User();
        anotherUser.setId(99L);
        category.setUser(anotherUser);

        BudgetController.CategoryBudgetRequest request = new BudgetController.CategoryBudgetRequest();
        request.categoryId = category.getId();
        request.month = 7;
        request.year = 2025;
        request.amount = BigDecimal.valueOf(1000);

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));

        ResponseEntity<?> response = budgetController.createCategoryBudget(request, authentication);
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void testCreateBudget_conflict() {
        BudgetController.CategoryBudgetRequest request = new BudgetController.CategoryBudgetRequest();
        request.categoryId = category.getId();
        request.month = 7;
        request.year = 2025;
        request.amount = BigDecimal.valueOf(1000);

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));
        when(budgetService.getByUserCategoryMonthYear(user.getId(), category.getId(), 7, 2025))
                .thenReturn(Optional.of(budget));

        ResponseEntity<?> response = budgetController.createCategoryBudget(request, authentication);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Budget already exists for this category/month/year", response.getBody());
    }

    @Test
    void testUpdateBudget_success() {
        BudgetController.CategoryBudgetRequest request = new BudgetController.CategoryBudgetRequest();
        request.categoryId = category.getId();
        request.month = 8;
        request.year = 2026;
        request.amount = BigDecimal.valueOf(1500);

        when(budgetService.getById(budget.getId())).thenReturn(Optional.of(budget));
        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));
        when(budgetService.updateBudget(any(Budget.class))).thenReturn(budget);

        ResponseEntity<?> response = budgetController.updateCategoryBudget(budget.getId(), request, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(budget, response.getBody());
    }

    @Test
    void testUpdateBudget_notOwnedByUser_forbidden() {
        User otherUser = new User();
        otherUser.setId(77L);
        budget.setUser(otherUser);

        BudgetController.CategoryBudgetRequest request = new BudgetController.CategoryBudgetRequest();
        request.categoryId = category.getId();
        request.month = 8;
        request.year = 2026;
        request.amount = BigDecimal.valueOf(1500);

        when(budgetService.getById(budget.getId())).thenReturn(Optional.of(budget));

        ResponseEntity<?> response = budgetController.updateCategoryBudget(budget.getId(), request, authentication);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void testGetAllBudgets_success() {
        when(budgetService.getBudgetsForUser(user.getId())).thenReturn(List.of(budget));

        ResponseEntity<List<Budget>> response = budgetController.getAllBudgets(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testDeleteBudget_success() {
        when(budgetService.getById(budget.getId())).thenReturn(Optional.of(budget));

        ResponseEntity<?> response = budgetController.deleteBudget(budget.getId(), authentication);

        assertEquals(204, response.getStatusCodeValue());
        verify(budgetService).deleteBudget(budget.getId());
    }

    @Test
    void testDeleteBudget_notFound() {
        when(budgetService.getById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = budgetController.deleteBudget(999L, authentication);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Budget not found", response.getBody());
    }

    @Test
    void testDeleteBudget_notOwnedByUser_forbidden() {
        User otherUser = new User();
        otherUser.setId(88L);
        budget.setUser(otherUser);

        when(budgetService.getById(budget.getId())).thenReturn(Optional.of(budget));

        ResponseEntity<?> response = budgetController.deleteBudget(budget.getId(), authentication);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }
}
