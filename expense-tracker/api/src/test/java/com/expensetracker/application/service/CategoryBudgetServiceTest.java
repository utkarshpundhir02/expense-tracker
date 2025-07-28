package com.expensetracker.application.service;

import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.User;
import com.expensetracker.domain.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryBudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private CategoryBudgetService budgetService;

    private User user;
    private Category category;
    private Budget budget;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        category = new Category();
        category.setId(10L);

        budget = new Budget();
        budget.setId(100L);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(new BigDecimal("5000"));
        budget.setMonth(7);
        budget.setYear(2025);
    }

    @Test
    void saveOrUpdateCategoryBudget_createsNewBudgetIfNotExists() {
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(user.getId(), category.getId(), 7, 2025))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget result = budgetService.saveOrUpdateCategoryBudget(user, category, 7, 2025, new BigDecimal("5000"));

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(category, result.getCategory());
        assertEquals(7, result.getMonth());
        assertEquals(2025, result.getYear());
        assertEquals(new BigDecimal("5000"), result.getAmount());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void saveOrUpdateCategoryBudget_updatesExistingBudget() {
        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(user.getId(), category.getId(), 7, 2025))
                .thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget result = budgetService.saveOrUpdateCategoryBudget(user, category, 7, 2025, new BigDecimal("10000"));

        assertEquals(new BigDecimal("10000"), result.getAmount());
        verify(budgetRepository, times(1)).save(budget);
    }

    @Test
    void getBudgetsForUser_returnsListOfBudgets() {
        when(budgetRepository.findAllByUserId(user.getId())).thenReturn(List.of(budget));

        List<Budget> result = budgetService.getBudgetsForUser(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(budget, result.get(0));
        verify(budgetRepository, times(1)).findAllByUserId(user.getId());
    }

    @Test
    void getById_returnsBudgetIfExists() {
        when(budgetRepository.findById(100L)).thenReturn(Optional.of(budget));

        Optional<Budget> result = budgetService.getById(100L);

        assertTrue(result.isPresent());
        assertEquals(budget, result.get());
        verify(budgetRepository, times(1)).findById(100L);
    }

    @Test
    void deleteBudget_callsRepositoryDeleteById() {
        doNothing().when(budgetRepository).deleteById(100L);

        budgetService.deleteBudget(100L);

        verify(budgetRepository, times(1)).deleteById(100L);
    }

//    @Test
//    void getByUserCategoryMonthYear_returnsExistingBudget() {
//        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 10L, 7, 2025))
//                .thenReturn(Optional.of(budget));
//
//        Optional<Budget> result = budgetService.getByUserCategoryMonthYear(1L, 10L, 7, 2025);
//
//        assertTrue(result.isPresent());
//        assertEquals(budget, result.get());
//        verify(budgetRepository, times(1))
//                .findByUserIdAndCategoryIdAndMonthAndYear(1L, 10L, 7, 2025);
//    }

    @Test
    void updateBudget_savesUpdatedBudget() {
        budget.setAmount(new BigDecimal("20000"));
        when(budgetRepository.save(budget)).thenReturn(budget);

        Budget result = budgetService.updateBudget(budget);

        assertEquals(new BigDecimal("20000"), result.getAmount());
        verify(budgetRepository, times(1)).save(budget);
    }
}
