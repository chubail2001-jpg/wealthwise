package com.wealthwise.service;

import com.wealthwise.dto.DashboardResponse;
import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    @Autowired private TransactionRepository txRepo;
    @Autowired private UserRepository userRepo;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public DashboardResponse getSummary() {
        User user   = currentUser();
        List<Transaction> allTx = txRepo.findByUserOrderByDateDesc(user);

        LocalDate today      = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd   = today.withDayOfMonth(today.lengthOfMonth());

        double monthIncome     = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.INCOME);
        double monthExpense    = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.EXPENSE);
        double monthSaving     = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.SAVING);
        double monthInvestment = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.INVESTMENT);

        double totalBalance = allTx.stream().mapToDouble(t -> {
            if (t.getType() == Transaction.TransactionType.INCOME)  return t.getAmount().doubleValue();
            if (t.getType() == Transaction.TransactionType.EXPENSE) return -t.getAmount().doubleValue();
            return 0;
        }).sum();

        return new DashboardResponse(totalBalance, monthIncome, monthExpense, monthSaving, monthInvestment);
    }

    private double sumType(List<Transaction> txs, LocalDate start, LocalDate end,
                           Transaction.TransactionType type) {
        return txs.stream()
                .filter(t -> t.getType() == type
                        && !t.getDate().isBefore(start)
                        && !t.getDate().isAfter(end))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }
}
