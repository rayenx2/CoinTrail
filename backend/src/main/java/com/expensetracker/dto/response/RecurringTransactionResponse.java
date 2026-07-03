package com.expensetracker.dto.response;

import com.expensetracker.entity.TransactionType;
import java.math.BigDecimal;

public class RecurringTransactionResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private BigDecimal amount;
    private String description;
    private TransactionType type;
    private Integer dayOfMonth;
    private boolean active;
    private String lastGeneratedPeriod;

    public RecurringTransactionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getLastGeneratedPeriod() { return lastGeneratedPeriod; }
    public void setLastGeneratedPeriod(String lastGeneratedPeriod) { this.lastGeneratedPeriod = lastGeneratedPeriod; }
}
