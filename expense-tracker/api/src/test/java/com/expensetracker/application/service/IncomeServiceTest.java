package com.expensetracker.application.service;

import com.expensetracker.domain.model.Income;
import com.expensetracker.domain.model.User;
import com.expensetracker.domain.repository.IncomeRepository;

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
public class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private IncomeService incomeService;

    private User user;
    private Income income;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        income = new Income();
        income.setId(1L);
        income.setAmount(BigDecimal.valueOf(1000));
        income.setDate(LocalDate.now());
        income.setSource("Job");
        income.setNotes("Salary");
        income.setUser(user);
    }

    @Test
    void testAddIncome() {
        when(incomeRepository.save(income)).thenReturn(income);

        Income saved = incomeService.addIncome(income);

        assertNotNull(saved);
        assertEquals(income.getId(), saved.getId());
        verify(incomeRepository, times(1)).save(income);
    }

    @Test
    void testGetIncomesByUser() {
        when(incomeRepository.findByUser(user)).thenReturn(List.of(income));

        List<Income> incomes = incomeService.getIncomesByUser(user);

        assertNotNull(incomes);
        assertEquals(1, incomes.size());
        assertEquals(user, incomes.get(0).getUser());
        verify(incomeRepository, times(1)).findByUser(user);
    }

    @Test
    void testGetIncomeById_found() {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));

        Optional<Income> result = incomeService.getIncomeById(1L);

        assertTrue(result.isPresent());
        assertEquals(income, result.get());
        verify(incomeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetIncomeById_notFound() {
        when(incomeRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Income> result = incomeService.getIncomeById(2L);

        assertFalse(result.isPresent());
        verify(incomeRepository, times(1)).findById(2L);
    }

    @Test
    void testDeleteIncome() {
        doNothing().when(incomeRepository).deleteById(1L);

        incomeService.deleteIncome(1L);

        verify(incomeRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateIncome() {
        income.setNotes("Updated salary");

        when(incomeRepository.save(income)).thenReturn(income);

        Income updated = incomeService.updateIncome(income);

        assertNotNull(updated);
        assertEquals("Updated salary", updated.getNotes());
        verify(incomeRepository, times(1)).save(income);
    }
}
