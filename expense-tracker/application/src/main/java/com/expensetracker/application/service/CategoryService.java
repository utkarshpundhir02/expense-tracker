package com.expensetracker.application.service;

import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.CategoryType;
import com.expensetracker.domain.model.Expense;
import com.expensetracker.domain.model.User;
import com.expensetracker.domain.repository.CategoryRepository;
import com.expensetracker.domain.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Optional<Category> findByNameAndTypeAndUser(String name , CategoryType type, User user){
    	return categoryRepository.findByNameAndTypeAndUser(name, type, user);
    }
    // Add a new category for a user
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    // List all categories for a user
    public List<Category> getCategoriesByUser(User user) {
        return categoryRepository.findByUser(user);
    }

    // Get a specific category by ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public List<Category> getCategoriesByTypeForUser(String typeStr, User user) {
        CategoryType type = CategoryType.valueOf(typeStr.toUpperCase());
        return categoryRepository.findByTypeAndUser(type, user);
    }

    // Update an category
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Delete an category by ID
    public void deleteCategory(Long id) {
    	categoryRepository.deleteById(id);
    }
}
