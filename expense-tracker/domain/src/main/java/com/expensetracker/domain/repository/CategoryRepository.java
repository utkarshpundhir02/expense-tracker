package com.expensetracker.domain.repository;

import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.CategoryType;
import com.expensetracker.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser(User user);
    Optional<Category> findByNameAndTypeAndUser(String name , CategoryType type, User user);
    List<Category> findByTypeAndUser(CategoryType type, User user);
 }
