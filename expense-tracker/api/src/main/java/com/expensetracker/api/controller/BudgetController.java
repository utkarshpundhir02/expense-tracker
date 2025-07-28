package com.expensetracker.api.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.application.service.CategoryBudgetService;
import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.User;

@RestController
@RequestMapping("/category-budgets")
public class BudgetController {

	@Autowired
    private final CategoryBudgetService budgetService;
    private final UserService userService;
    private final CategoryService categoryService;

    public BudgetController(CategoryBudgetService budgetService, UserService userService, CategoryService categoryService) {
        this.budgetService = budgetService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    // DTO
    public static class CategoryBudgetRequest {
        @NotNull
        public Integer month;

        @NotNull
        public Integer year;

        @NotNull
        public BigDecimal amount;

        @NotNull
        public Long categoryId;
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<?> createCategoryBudget(
            @RequestBody CategoryBudgetRequest request,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);

        Category category = categoryService.getCategoryById(request.categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        // Check if budget already exists
        Optional<Budget> existing = budgetService.getByUserCategoryMonthYear(
                user.getId(), category.getId(), request.month, request.year);
        
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Budget already exists for this category/month/year");
        }

        Budget budget = budgetService.saveOrUpdateCategoryBudget(user, category, request.month, request.year, request.amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(budget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategoryBudget(
    		@PathVariable("id") Long id,
            @RequestBody CategoryBudgetRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        Budget budget = budgetService.getById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Category category = categoryService.getCategoryById(request.categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        budget.setAmount(request.amount);
        budget.setMonth(request.month);
        budget.setYear(request.year);
        budget.setCategory(category);

        Budget updated = budgetService.updateBudget(budget);
        return ResponseEntity.ok(updated);
    }


    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Budget> budgets = budgetService.getBudgetsForUser(user.getId());
        return ResponseEntity.ok(budgets);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable("id") Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);

        Optional<Budget> optional = budgetService.getById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Budget not found");
        }

        Budget budget = optional.get();
        if (!budget.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
