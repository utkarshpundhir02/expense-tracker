package com.expensetracker.api.controller;

import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.ExpenseService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.Expense;
import com.expensetracker.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseControllerTest {

    @InjectMocks
    private ExpenseController expenseController;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private Authentication authentication;

    private User testUser;
    private Category testCategory;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Food");
        testCategory.setUser(testUser);

        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setAmount(BigDecimal.valueOf(100));
        testExpense.setCategory(testCategory);
        testExpense.setDate(LocalDate.now());
        testExpense.setNotes("Lunch");
        testExpense.setUser(testUser);

        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userService.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
    }

    @Test
    void testAddExpense_success() {
        ExpenseController.ExpenseRequest request = new ExpenseController.ExpenseRequest();
        request.amount = BigDecimal.valueOf(100);
        request.categoryId = 1L;
        request.date = LocalDate.now().toString();
        request.notes = "Lunch";

        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(testCategory));
        when(expenseService.addExpense(any())).thenReturn(testExpense);

        ResponseEntity<?> response = expenseController.addExpense(request, authentication);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(testExpense, response.getBody());
    }

    @Test
    void testAddExpense_forbiddenCategory() {
        User anotherUser = new User();
        anotherUser.setId(99L);
        testCategory.setUser(anotherUser);

        ExpenseController.ExpenseRequest request = new ExpenseController.ExpenseRequest();
        request.amount = BigDecimal.valueOf(100);
        request.categoryId = 1L;
        request.date = LocalDate.now().toString();
        request.notes = "Lunch";

        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(testCategory));

        ResponseEntity<?> response = expenseController.addExpense(request, authentication);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied to category", response.getBody());
    }

    @Test
    void testGetExpenses() {
        when(expenseService.getExpensesByUser(testUser)).thenReturn(List.of(testExpense));

        ResponseEntity<List<Expense>> response = expenseController.getExpenses(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetExpenseById_success() {
        when(expenseService.getExpenseById(1L)).thenReturn(Optional.of(testExpense));

        ResponseEntity<?> response = expenseController.getExpenseById(1L, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testExpense, response.getBody());
    }

    @Test
    void testGetExpenseById_notFound() {
        when(expenseService.getExpenseById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = expenseController.getExpenseById(1L, authentication);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Expense not found", response.getBody());
    }

    @Test
    void testGetExpenseById_forbidden() {
        User otherUser = new User();
        otherUser.setId(99L);
        testExpense.setUser(otherUser);

        when(expenseService.getExpenseById(1L)).thenReturn(Optional.of(testExpense));

        ResponseEntity<?> response = expenseController.getExpenseById(1L, authentication);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void testUpdateExpense_success() {
        ExpenseController.ExpenseRequest request = new ExpenseController.ExpenseRequest();
        request.amount = BigDecimal.valueOf(200);
        request.categoryId = 1L;
        request.date = LocalDate.now().toString();
        request.notes = "Updated";

        when(expenseService.getExpenseById(1L)).thenReturn(Optional.of(testExpense));
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(testCategory));
        when(expenseService.updateExpense(any())).thenReturn(testExpense);

        ResponseEntity<?> response = expenseController.updateExpense(1L, request, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(testExpense, response.getBody());
    }

    @Test
    void testUpdateExpense_notFound() {
        ExpenseController.ExpenseRequest request = new ExpenseController.ExpenseRequest();
        request.categoryId = 1L;

        when(expenseService.getExpenseById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = expenseController.updateExpense(1L, request, authentication);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Expense not found", response.getBody());
    }

    @Test
    void testUpdateExpense_forbidden() {
        User otherUser = new User();
        otherUser.setId(999L);
        testExpense.setUser(otherUser);

        ExpenseController.ExpenseRequest request = new ExpenseController.ExpenseRequest();
        request.categoryId = 1L;

        when(expenseService.getExpenseById(1L)).thenReturn(Optional.of(testExpense));

        ResponseEntity<?> response = expenseController.updateExpense(1L, request, authentication);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void testDeleteExpense_success() {
        when(expenseService.getExpenseById(1L)).thenReturn(Optional.of(testExpense));

        ResponseEntity<?> response = expenseController.deleteExpense(1L, authentication);

        assertEquals(204, response.getStatusCodeValue());
        verify(expenseService).deleteExpense(1L);
    }

    @Test
    void testDeleteExpense_notFound() {
        when(expenseService.getExpenseById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = expenseController.deleteExpense(1L, authentication);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Expense not found", response.getBody());
    }

    @Test
    void testDeleteExpense_forbidden() {
        User otherUser = new User();
        otherUser.setId(999L);
        testExpense.setUser(otherUser);

        when(expenseService.getExpenseById(1L)).thenReturn(Optional.of(testExpense));

        ResponseEntity<?> response = expenseController.deleteExpense(1L, authentication);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody());
    }
}
