package com.expensetracker.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.expensetracker")
@EnableJpaRepositories(basePackages = "com.expensetracker.domain.repository")
@EntityScan(basePackages = "com.expensetracker.domain.model")
public class ExpenseTrackerApplication {
	public static void main(String[] args) {
		SpringApplication.run(ExpenseTrackerApplication.class, args);
		System.out.println("Application running successful");
	}
}