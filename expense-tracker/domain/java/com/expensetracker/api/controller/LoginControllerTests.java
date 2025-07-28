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
class LoginControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	JwtTokenProvider jwtTokenProvider;

	@Test
	void loginUser_success() throws Exception {
		String name = "name";
		String email = "test@example.com";
		String passwordHash = "test123";

		String jsonRequest = """
				    {
				      "email": "test@example.com",
				      "password": "test123"
				    }
				""";

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash(passwordHash);
		
		System.out.println(user);
		
		Mockito.when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
		Mockito.when(passwordEncoder.encode("test123")).thenReturn("test123");
		Mockito.when(passwordEncoder.matches("test123", "test123")).thenReturn(true);
		Mockito.when(jwtTokenProvider.generateToken(email)).thenReturn("jwt-token");

		mockMvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
				.andExpect(status().isOk()).andReturn();
	}

	@Test
	void loginUser_failure() throws Exception {
		String name = "name";
		String email = "test@example.com";
		String passwordHash = "test123";

		String jsonRequest = """
				    {
				      "email": "test@example.com",
				      "password": "test123"
				    }
				""";

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash(passwordHash);
		
		System.out.println(user);
		
		Mockito.when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
		Mockito.when(passwordEncoder.encode("test123")).thenReturn("test123");
		Mockito.when(passwordEncoder.matches("test123", "test123")).thenReturn(true);
		Mockito.when(jwtTokenProvider.generateToken(email)).thenReturn("jwt-token");

		mockMvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(jsonRequest))
				.andExpect(status().isUnauthorized()).andReturn();
	}

}