package com.expensetracker.scheduler;

import com.expensetracker.service.RecurringTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RecurringTransactionScheduler.class);

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 5 0 * * ?")
    public void generateDueRecurringTransactions() {
        logger.info("Running daily recurring-transaction generation...");
        int generated = recurringTransactionService.generateDueTransactions();
        logger.info("Recurring-transaction run complete — {} transaction(s) generated", generated);
    }
}
