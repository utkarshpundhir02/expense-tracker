package com.expensetracker.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.expensetracker.application.service.IncomeService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.Income;
import com.expensetracker.domain.model.User;

@ExtendWith(MockitoExtension.class)
class IncomeControllerTest {

    @Mock
    private IncomeService incomeService;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private IncomeController incomeController;

    private User user;
    private Category category;
    private Income income;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        category = new Category();
        category.setId(10L);
        category.setName("Salary");
        category.setUser(user);

        income = new Income();
        income.setId(100L);
        income.setUser(user);
        income.setCategory(category);
        income.setAmount(BigDecimal.valueOf(5000));
        income.setSource("Job");
        income.setNotes("Monthly salary");
        income.setDate(LocalDate.now());

        when(authentication.getName()).thenReturn(user.getEmail());
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void testAddIncome_success() {
        IncomeController.IncomeRequest request = new IncomeController.IncomeRequest();
        request.amount = income.getAmount();
        request.categoryId = category.getId();
        request.source = income.getSource();
        request.notes = income.getNotes();
        request.date = income.getDate();

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));
        when(incomeService.addIncome(any())).thenReturn(income);

        ResponseEntity<?> response = incomeController.addIncome(request, authentication);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(income, response.getBody());
    }

    @Test
    void testAddIncome_categoryNotFound() {
        IncomeController.IncomeRequest request = new IncomeController.IncomeRequest();
        request.categoryId = 999L;

        when(categoryService.getCategoryById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> incomeController.addIncome(request, authentication));
    }

    @Test
    void testAddIncome_accessDeniedToCategory() {
        User anotherUser = new User();
        anotherUser.setId(99L);

        category.setUser(anotherUser); // different user
        IncomeController.IncomeRequest request = new IncomeController.IncomeRequest();
        request.categoryId = category.getId();

        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));

        ResponseEntity<?> response = incomeController.addIncome(request, authentication);
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied to category", response.getBody());
    }

    @Test
    void testGetAllIncomes_success() {
        when(incomeService.getIncomesByUser(user)).thenReturn(List.of(income));

        ResponseEntity<List<Income>> response = incomeController.getAllIncomes(authentication);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetIncomeById_success() {
        when(incomeService.getIncomeById(income.getId())).thenReturn(Optional.of(income));

        ResponseEntity<?> response = incomeController.getIncomeById(income.getId(), authentication);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(income, response.getBody());
    }

    @Test
    void testGetIncomeById_notFound() {
        when(incomeService.getIncomeById(404L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = incomeController.getIncomeById(404L, authentication);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Income not found", response.getBody());
    }

    @Test
    void testGetIncomeById_accessDenied() {
        User anotherUser = new User();
        anotherUser.setId(999L);
        income.setUser(anotherUser);

        when(incomeService.getIncomeById(income.getId())).thenReturn(Optional.of(income));

        ResponseEntity<?> response = incomeController.getIncomeById(income.getId(), authentication);
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void testUpdateIncome_success() {
        IncomeController.IncomeRequest request = new IncomeController.IncomeRequest();
        request.amount = BigDecimal.valueOf(7000);
        request.source = "Freelance";
        request.date = LocalDate.now();
        request.notes = "Side project";
        request.categoryId = category.getId();

        when(incomeService.getIncomeById(income.getId())).thenReturn(Optional.of(income));
        when(categoryService.getCategoryById(category.getId())).thenReturn(Optional.of(category));
        when(incomeService.updateIncome(any())).thenReturn(income);

        ResponseEntity<?> response = incomeController.updateIncome(income.getId(), request, authentication);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(income, response.getBody());
    }

    @Test
    void testUpdateIncome_notFound() {
        IncomeController.IncomeRequest request = new IncomeController.IncomeRequest();
        when(incomeService.getIncomeById(404L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = incomeController.updateIncome(404L, request, authentication);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Income not found", response.getBody());
    }

    @Test
    void testDeleteIncome_success() {
        when(incomeService.getIncomeById(income.getId())).thenReturn(Optional.of(income));

        ResponseEntity<?> response = incomeController.deleteIncome(income.getId(), authentication);
        assertEquals(204, response.getStatusCodeValue());
        verify(incomeService, times(1)).deleteIncome(income.getId());
    }

    @Test
    void testDeleteIncome_notFound() {
        when(incomeService.getIncomeById(404L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = incomeController.deleteIncome(404L, authentication);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Income not found", response.getBody());
    }

    @Test
    void testDeleteIncome_accessDenied() {
        User anotherUser = new User();
        anotherUser.setId(99L);
        income.setUser(anotherUser);

        when(incomeService.getIncomeById(income.getId())).thenReturn(Optional.of(income));

        ResponseEntity<?> response = incomeController.deleteIncome(income.getId(), authentication);
        assertEquals(404, response.getStatusCodeValue()); // since it won't match `.filter(income -> income.getUser().getId().equals(user.getId()))`
        assertEquals("Income not found", response.getBody());
    }
}
