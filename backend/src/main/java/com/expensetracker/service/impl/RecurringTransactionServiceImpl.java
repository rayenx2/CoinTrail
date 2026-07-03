package com.expensetracker.service.impl;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.request.TransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.RecurringTransactionMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.RecurringTransactionRepository;
import com.expensetracker.service.RecurringTransactionService;
import com.expensetracker.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionServiceImpl.class);

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RecurringTransactionMapper recurringTransactionMapper;

    @Autowired
    private TransactionService transactionService;

    @Override
    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request, User user) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(user);
        recurring.setCategory(category);
        recurring.setAmount(request.getAmount());
        recurring.setDescription(request.getDescription());
        recurring.setType(request.getType());
        recurring.setDayOfMonth(request.getDayOfMonth());
        recurring.setActive(true);

        recurring = recurringTransactionRepository.save(recurring);
        return recurringTransactionMapper.toResponse(recurring);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getAll(User user) {
        return recurringTransactionRepository.findByUserOrderByDayOfMonthAsc(user).stream()
                .map(recurringTransactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id, User user) {
        RecurringTransaction recurring = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));

        if (!recurring.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this recurring transaction");
        }

        recurringTransactionRepository.delete(recurring);
    }

    @Override
    @Transactional
    public int generateDueTransactions() {
        LocalDate today = LocalDate.now();
        YearMonth currentPeriod = YearMonth.from(today);
        int generated = 0;

        for (RecurringTransaction recurring : recurringTransactionRepository.findByActiveTrue()) {
            if (recurring.alreadyGeneratedFor(currentPeriod)) continue;
            if (recurring.getDayOfMonth() > today.getDayOfMonth()) continue;

            TransactionRequest request = new TransactionRequest();
            request.setCategoryId(recurring.getCategory().getId());
            request.setAmount(recurring.getAmount());
            request.setDescription(recurring.getDescription());
            request.setTransactionDate(currentPeriod.atDay(Math.min(recurring.getDayOfMonth(), currentPeriod.lengthOfMonth())));
            request.setType(recurring.getType());

            transactionService.createTransaction(request, recurring.getUser());

            recurring.setLastGeneratedPeriod(currentPeriod.toString());
            recurringTransactionRepository.save(recurring);
            generated++;

            log.info("Generated recurring transaction for user {} category {} period {}",
                    recurring.getUser().getId(), recurring.getCategory().getName(), currentPeriod);
        }

        return generated;
    }
}
