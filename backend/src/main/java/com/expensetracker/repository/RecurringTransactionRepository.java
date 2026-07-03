package com.expensetracker.repository;

import com.expensetracker.entity.RecurringTransaction;
import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserOrderByDayOfMonthAsc(User user);
    List<RecurringTransaction> findByActiveTrue();
    Optional<RecurringTransaction> findByIdAndUser(Long id, User user);
}
