package com.expensetracker.mapper;

import com.expensetracker.dto.response.RecurringTransactionResponse;
import com.expensetracker.entity.RecurringTransaction;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionMapper {
    public RecurringTransactionResponse toResponse(RecurringTransaction r) {
        if (r == null) return null;
        RecurringTransactionResponse response = new RecurringTransactionResponse();
        response.setId(r.getId());
        response.setCategoryId(r.getCategory() != null ? r.getCategory().getId() : null);
        response.setCategoryName(r.getCategory() != null ? r.getCategory().getName() : null);
        response.setCategoryIcon(r.getCategory() != null ? r.getCategory().getIcon() : null);
        response.setAmount(r.getAmount());
        response.setDescription(r.getDescription());
        response.setType(r.getType());
        response.setDayOfMonth(r.getDayOfMonth());
        response.setActive(r.isActive());
        response.setLastGeneratedPeriod(r.getLastGeneratedPeriod());
        return response;
    }
}
