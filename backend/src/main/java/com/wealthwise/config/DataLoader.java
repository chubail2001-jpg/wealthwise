package com.wealthwise.config;

import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired private UserRepository userRepo;
    @Autowired private TransactionRepository txRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Skip if seed data already exists
        if (userRepo.existsByUsername("demo")) return;

        User demo = new User("demo", passwordEncoder.encode("password123"), "Demo User", "demo@wealthwise.com");
        userRepo.save(demo);

        LocalDate today = LocalDate.now();

        // Seed 3 months of realistic transactions
        for (int m = 2; m >= 0; m--) {
            LocalDate base = today.minusMonths(m);

            // Income
            addTx(demo, Transaction.TransactionType.INCOME,     5000.00, "Monthly Salary",       "Work",      base.withDayOfMonth(1));
            addTx(demo, Transaction.TransactionType.INCOME,      800.00, "Freelance Project",     "Work",      base.withDayOfMonth(10));

            // Expenses
            addTx(demo, Transaction.TransactionType.EXPENSE,    1200.00, "Rent",                  "Housing",   base.withDayOfMonth(2));
            addTx(demo, Transaction.TransactionType.EXPENSE,     350.00, "Groceries",             "Food",      base.withDayOfMonth(7));
            addTx(demo, Transaction.TransactionType.EXPENSE,     120.00, "Electricity & Internet","Bills",     base.withDayOfMonth(12));
            addTx(demo, Transaction.TransactionType.EXPENSE,      85.00, "Dining Out",            "Food",      base.withDayOfMonth(15));
            addTx(demo, Transaction.TransactionType.EXPENSE,      45.00, "Streaming Services",    "Lifestyle", base.withDayOfMonth(18));

            // Savings
            addTx(demo, Transaction.TransactionType.SAVING,      500.00, "Emergency Fund",        "Savings",   base.withDayOfMonth(3));
            addTx(demo, Transaction.TransactionType.SAVING,      300.00, "Vacation Fund",         "Savings",   base.withDayOfMonth(20));

            // Investments
            addTx(demo, Transaction.TransactionType.INVESTMENT,  600.00, "S&P 500 ETF",           "Stocks",    base.withDayOfMonth(5));
            addTx(demo, Transaction.TransactionType.INVESTMENT,  200.00, "Bitcoin",               "Crypto",    base.withDayOfMonth(18));
        }
    }

    private void addTx(User user, Transaction.TransactionType type, double amount,
                       String description, String category, LocalDate date) {
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setType(type);
        tx.setAmount(BigDecimal.valueOf(amount));
        tx.setDescription(description);
        tx.setCategory(category);
        tx.setDate(date);
        txRepo.save(tx);
    }
}
