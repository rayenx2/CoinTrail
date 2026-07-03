package com.expensetracker.controller;

import com.expensetracker.dto.request.RecurringTransactionRequest;
import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
@Tag(name = "Recurring Transactions", description = "Endpoints for managing monthly recurring income/expense rules (rent, subscriptions, salary)")
@SecurityRequirement(name = "bearerAuth")
public class RecurringTransactionController {

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a recurring transaction rule", description = "The transaction will auto-generate every month on the given day")
    public ResponseEntity<RecurringTransactionResponse> create(
            @Valid @RequestBody RecurringTransactionRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(recurringTransactionService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "List recurring transaction rules for the current user")
    public ResponseEntity<List<RecurringTransactionResponse>> getAll(
            @Parameter(hidden = true) Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(recurringTransactionService.getAll(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (stop) a recurring transaction rule")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        User user = currentUser(authentication);
        recurringTransactionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).get();
    }
}
