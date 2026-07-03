package com.expensetracker.dto.request;

import com.expensetracker.entity.TransactionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class RecurringTransactionRequest {
    @NotNull
    private Long categoryId;
    @NotNull @Positive
    private BigDecimal amount;
    private String description;
    @NotNull
    private TransactionType type;
    @NotNull @Min(1) @Max(28)
    private Integer dayOfMonth;

    public RecurringTransactionRequest() {}
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }
}
