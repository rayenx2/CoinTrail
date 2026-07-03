package com.expensetracker.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Integer dayOfMonth;

    @Column(nullable = false)
    private boolean active = true;

    /** Year-month (e.g. "2026-07") the last auto-generated transaction covers. Null until first run. */
    private String lastGeneratedPeriod;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public RecurringTransaction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean alreadyGeneratedFor(YearMonth period) {
        return period.toString().equals(lastGeneratedPeriod);
    }
}
