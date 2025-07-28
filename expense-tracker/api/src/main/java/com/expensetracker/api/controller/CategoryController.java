package com.expensetracker.api.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

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

import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.config.exception.ResourceNotFoundException;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.CategoryType;
import com.expensetracker.domain.model.User;

@RestController
@RequestMapping("/category")
public class CategoryController {

	private final CategoryService categoryService;
	private final UserService userService;

	public CategoryController(CategoryService categoryService, UserService userService) {
		this.categoryService = categoryService;
		this.userService = userService;
	}

	// DTO for creating/updating Category
	public static class CategoryRequest {
		public String name;
		public String type;
	}

	// Helper method to get the authenticated User object
	private User getCurrentUser(Authentication authentication) {
		String email = authentication.getName();
		return userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
	}

	@PostMapping
	public ResponseEntity<?> addCategory(@RequestBody CategoryRequest request, Authentication authentication) {
		User user = getCurrentUser(authentication);

		Category category = new Category();
		category.setName(request.name);
		category.setType(CategoryType.valueOf(request.type.toUpperCase()));
		category.setUser(user);

		Optional<Category> existing = categoryService.findByNameAndTypeAndUser(category.getName(),
				category.getType(), category.getUser());

		if (existing.isPresent()) {
			throw new IllegalArgumentException("Category with same name and type already exists for this user");
		}

		Category savedCategory = categoryService.addCategory(category);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
	}

	@GetMapping
	public ResponseEntity<List<Category>> getCategories(Authentication authentication) {
		User user = getCurrentUser(authentication);
		List<Category> categories = categoryService.getCategoriesByUser(user);
		return ResponseEntity.ok(categories);
	}
	
	@GetMapping("/type/{type}")
    public ResponseEntity<List<Category>> getCategoriesByType(
            @PathVariable("type") String type,
            Authentication authentication) {

        // Extract user ID from authentication
		User user = getCurrentUser(authentication);

        List<Category> categories = categoryService.getCategoriesByTypeForUser(type.toUpperCase(), user);
        return ResponseEntity.ok(categories);
    }

	@GetMapping("/{id}")
	public ResponseEntity<?> getCategoryById(@PathVariable("id") Long id, Authentication authentication)
			throws AccessDeniedException {
		User user = getCurrentUser(authentication);
		Optional<Category> optionalCategory = categoryService.getCategoryById(id);

		if (optionalCategory.isEmpty()) {
			throw new ResourceNotFoundException("Category not found");
		}

		Category category = optionalCategory.get();
		if (!category.getUser().getId().equals(user.getId())) {
			throw new AccessDeniedException("Access denied");
		}

		return ResponseEntity.ok(category);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateCategory(@PathVariable("id") Long id, @RequestBody CategoryRequest request,
			Authentication authentication) {
		User user = getCurrentUser(authentication);
		Optional<Category> optionalCategory = categoryService.getCategoryById(id);

		if (optionalCategory.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
		}

		Category category = optionalCategory.get();
		if (!category.getUser().getId().equals(user.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
		}

		category.setName(request.name);
		category.setType(CategoryType.valueOf(request.type.toUpperCase()));

		Category updatedCategory = categoryService.updateCategory(category);
		return ResponseEntity.ok(updatedCategory);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id, Authentication authentication) {
		User user = getCurrentUser(authentication);
		Optional<Category> optionalCategory = categoryService.getCategoryById(id);

		if (optionalCategory.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
		}

		Category category = optionalCategory.get();
		if (!category.getUser().getId().equals(user.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
		}

		categoryService.deleteCategory(id);

		return ResponseEntity.noContent().build();
	}
}
