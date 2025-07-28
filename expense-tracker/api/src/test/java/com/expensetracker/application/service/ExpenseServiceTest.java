package com.expensetracker.application.service;

import com.expensetracker.domain.model.Expense;
import com.expensetracker.domain.model.User;
import com.expensetracker.domain.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense expense;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        expense = new Expense();
        expense.setId(1L);
        expense.setAmount(BigDecimal.valueOf(150));
        expense.setDate(LocalDate.now());
        expense.setNotes("Lunch");
        expense.setUser(user);
    }

    @Test
    void testAddExpense() {
        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense saved = expenseService.addExpense(expense);

        assertNotNull(saved);
        assertEquals(expense.getId(), saved.getId());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void testGetExpensesByUser() {
        when(expenseRepository.findByUser(user)).thenReturn(List.of(expense));

        List<Expense> result = expenseService.getExpensesByUser(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getUser().getId());
        verify(expenseRepository, times(1)).findByUser(user);
    }

    @Test
    void testGetExpenseById_found() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        Optional<Expense> result = expenseService.getExpenseById(1L);

        assertTrue(result.isPresent());
        assertEquals(expense, result.get());
        verify(expenseRepository, times(1)).findById(1L);
    }

    @Test
    void testGetExpenseById_notFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Expense> result = expenseService.getExpenseById(99L);

        assertFalse(result.isPresent());
        verify(expenseRepository, times(1)).findById(99L);
    }

    @Test
    void testUpdateExpense() {
        expense.setNotes("Updated note");

        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense updated = expenseService.updateExpense(expense);

        assertEquals("Updated note", updated.getNotes());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void testDeleteExpense() {
        doNothing().when(expenseRepository).deleteById(1L);

        expenseService.deleteExpense(1L);

        verify(expenseRepository, times(1)).deleteById(1L);
    }
}
