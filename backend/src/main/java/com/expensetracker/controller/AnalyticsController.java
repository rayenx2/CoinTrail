package com.expensetracker.controller;

import com.expensetracker.dto.response.AnalyticsSummaryResponse;
import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Endpoints for dashboard analytics: spending trends, category breakdowns and savings rate")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/summary")
    @Operation(summary = "Get analytics summary", description = "Returns total income/expense, net balance, savings rate, " +
            "top expense categories and a monthly income vs expense trend for the requested window.")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(
            @Parameter(description = "Number of months to include in the trend (default 6, max 24)")
            @RequestParam(name = "months", defaultValue = "6") int months,
            @Parameter(hidden = true) Authentication authentication) {

        int safeMonths = Math.max(1, Math.min(months, 24));
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        return ResponseEntity.ok(analyticsService.getSummary(user, safeMonths));
    }
}
