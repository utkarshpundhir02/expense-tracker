package com.expensetracker.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.User;
import com.expensetracker.domain.repository.BudgetRepository;

@Service
public class CategoryBudgetService {

    private final BudgetRepository categoryBudgetRepository;

    public CategoryBudgetService(BudgetRepository categoryBudgetRepository) {
        this.categoryBudgetRepository = categoryBudgetRepository;
    }

    public Budget saveOrUpdateCategoryBudget(User user, Category category, int month, int year, BigDecimal amount) {
        Optional<Budget> existing = categoryBudgetRepository
                .findByUserIdAndCategoryIdAndMonthAndYear(user.getId(), category.getId(), month, year);

        Budget budget = existing.orElseGet(Budget::new);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setMonth(month);
        budget.setYear(year);
        budget.setAmount(amount);

        return categoryBudgetRepository.save(budget);
    }

    public List<Budget> getBudgetsForUser(Long userId) {
        return categoryBudgetRepository.findAllByUserId(userId);
    }

    public Optional<Budget> getById(Long id) {
        return categoryBudgetRepository.findById(id);
    }

    public void deleteBudget(Long id) {
        categoryBudgetRepository.deleteById(id);
    }

	public Optional<Budget> getByUserCategoryMonthYear(Long id, Long id2, Integer month, Integer year) {
		// TODO Auto-generated method stub
		return categoryBudgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(id, id2, month, year);
	}

	public Budget updateBudget(Budget budget) {
		return categoryBudgetRepository.save(budget);
	}
}
