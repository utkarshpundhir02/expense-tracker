package com.expensetracker.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expensetracker.domain.model.Income;
import com.expensetracker.domain.model.User;
import com.expensetracker.domain.repository.IncomeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class IncomeService {

	@Autowired
    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public Income addIncome(Income income) {
        return incomeRepository.save(income);
    }

    public List<Income> getIncomesByUser(User user) {
        return incomeRepository.findByUser(user);
    }

    public Optional<Income> getIncomeById(Long id) {
        return incomeRepository.findById(id);
    }

    public void deleteIncome(Long id) {
        incomeRepository.deleteById(id);
    }

    public Income updateIncome(Income income) {
        return incomeRepository.save(income);
    }
}
