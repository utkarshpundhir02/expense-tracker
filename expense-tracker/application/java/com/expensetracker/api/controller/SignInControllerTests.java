package com.expensetracker.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.expensetracker.application.service.CategoryService;
import com.expensetracker.application.service.UserService;
import com.expensetracker.domain.model.User;
import com.expensetracker.infrastructure.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SignInControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private CategoryService categoryService;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	JwtTokenProvider jwtTokenProvider;

	@Test
	void registerUser_success() throws Exception {
		String name = "User1";
		String email = "test@example.com";
		String passwordHash = "encoded-password";

		String jsonRequest = """
				    {
				      "name": "User1",
				      "email": "test@example.com",
				      "password": "test123"
				    }
				""";

		Mockito.when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
		Mockito.when(passwordEncoder.encode("test123")).thenReturn("encoded-password");

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash(passwordHash);

		Mockito.when(userService.registerUser(name, email, passwordHash)).thenReturn(user);

		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
				.andExpect(status().isCreated()).andReturn();
	}

	@Test
	void registerUser_failure() throws Exception {
		String name = "User1";
		String email = "test@example.com";
		String passwordHash = "encoded-password";

		String jsonRequest = """
				    {
				       "name": "User1",
				      "email": "test@example.com",
				      "password": "test123"
				    }
				""";

		Mockito.when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
		Mockito.when(passwordEncoder.encode("test123")).thenReturn("encoded-password");

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash(passwordHash);

		Mockito.when(userService.registerUser(name, email, passwordHash)).thenThrow(new IllegalArgumentException("Invalid user input"));

		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
				.andExpect(status().isBadRequest()).andReturn();
	}

}