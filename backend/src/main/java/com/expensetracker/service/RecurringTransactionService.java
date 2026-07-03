package com.expensetracker.service;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.User;
import java.util.List;

public interface RecurringTransactionService {
    RecurringTransactionResponse create(RecurringTransactionRequest request, User user);
    List<RecurringTransactionResponse> getAll(User user);
    void delete(Long id, User user);
    int generateDueTransactions();
}
