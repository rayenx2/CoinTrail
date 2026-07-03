package com.expensetracker.service;

import com.expensetracker.dto.response.AnalyticsSummaryResponse;
import com.expensetracker.entity.User;

public interface AnalyticsService {
    AnalyticsSummaryResponse getSummary(User user, int months);
}
