package com.expensetracker.api.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.IncomeService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.config.exception.ResourceNotFoundException;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.Expense;
import com.expensetracker.domain.model.Income;
import com.expensetracker.domain.model.User;

@RestController
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;
    private final UserService userService;
    private final CategoryService categoryService;

    public IncomeController(IncomeService incomeService, UserService userService, CategoryService categoryService) {
        this.incomeService = incomeService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    // ✅ DTO class for request
    public static class IncomeRequest {
        public Long categoryId;
        public String source;
        public String notes;
        public java.math.BigDecimal amount;
        public java.time.LocalDate date;
    }

    // ✅ Helper to fetch user
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // or (String) authentication.getPrincipal();
        return userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ Create Income with category
    @PostMapping
    public ResponseEntity<?> addIncome(@RequestBody IncomeRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);

        Category category = categoryService.getCategoryById(request.categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to category");
        }

        Income income = new Income();
        income.setAmount(request.amount);
        income.setDate(request.date);
        income.setNotes(request.notes);
        income.setSource(request.source);
        income.setCategory(category);
        income.setUser(user);

        Income savedIncome = incomeService.addIncome(income);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIncome);
    }

    @GetMapping
    public ResponseEntity<List<Income>> getAllIncomes(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Income> incomes = incomeService.getIncomesByUser(user);
        return ResponseEntity.ok(incomes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIncomeById(@PathVariable("id") Long id, Authentication authentication) {
    	User user = getCurrentUser(authentication);
        Optional<Income> optionalIncome = incomeService.getIncomeById(id);

        if (optionalIncome.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Income not found");
        }

        Income income = optionalIncome.get();
        if (!income.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return ResponseEntity.ok(income);
    }

    // ✅ Update income (also update category)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncome(@PathVariable("id") Long id, @RequestBody IncomeRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);

        Optional<Income> optionalIncome = incomeService.getIncomeById(id);
        if (optionalIncome.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Income not found");
        }

        Income income = optionalIncome.get();
        if (!income.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Category category = categoryService.getCategoryById(request.categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to category");
        }

        income.setAmount(request.amount);
        income.setSource(request.source);
        income.setDate(request.date);
        income.setNotes(request.notes);
        income.setCategory(category);

        Income updatedIncome = incomeService.updateIncome(income);
        return ResponseEntity.ok(updatedIncome);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncome(@PathVariable("id") Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);

        return incomeService.getIncomeById(id)
            .filter(income -> income.getUser().getId().equals(user.getId()))
            .map(income -> {
                incomeService.deleteIncome(id);
                return ResponseEntity.noContent().build();
            }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Income not found"));
    }
}
