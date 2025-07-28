package com.expensetracker.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.api.dto.UserResponseDTO;
import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.CategoryType;
import com.expensetracker.domain.model.User;

@RestController
@RequestMapping("/users")
public class SignInController {

	private final UserService userService;
	private final CategoryService categoryService;

//	@Autowired
	private PasswordEncoder passwordEncoder;

	public SignInController(UserService userService, CategoryService categoryService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.categoryService = categoryService;
		this.passwordEncoder = passwordEncoder;
	}

	// DTO for user registration request
	public static class RegisterRequest {
		public String name;
		public String email;
		public String password;
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
		try {
			Optional<User> existingUser = userService.findByEmail(request.email);

			if (!existingUser.isEmpty()) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
			}
			String passwordHash = passwordEncoder.encode(request.password);
			User user = userService.registerUser(request.name, request.email, passwordHash);
			UserResponseDTO response = new UserResponseDTO(user.getName(), user.getEmail());

			addGlobalCategories(user, categoryService);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);	
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	private static void addGlobalCategories(User user, CategoryService categoryService) {
		List<String[]> globalCategories = new ArrayList<>();

		// Add expense categories
		globalCategories.add(new String[] { "Food", "EXPENSE" });
		globalCategories.add(new String[] { "Transport", "EXPENSE" });
		globalCategories.add(new String[] { "Rent", "EXPENSE" });
		globalCategories.add(new String[] { "Utilities", "EXPENSE" });
		globalCategories.add(new String[] { "Entertainment", "EXPENSE" });

		// Add income categories
		globalCategories.add(new String[] { "Salary", "INCOME" });
		globalCategories.add(new String[] { "Freelancing", "INCOME" });
		globalCategories.add(new String[] { "Investments", "INCOME" });
		globalCategories.add(new String[] { "Rental Income", "INCOME" });
		globalCategories.add(new String[] { "Other", "INCOME" });

		for (String[] categoryArr : globalCategories) {
			Category category = new Category();
			category.setName(categoryArr[0]);
			category.setType(CategoryType.valueOf(categoryArr[1].toUpperCase()));
			category.setUser(user);

			Category savedCategory = categoryService.addCategory(category);
		}
	}
}
