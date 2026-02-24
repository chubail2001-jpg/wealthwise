package com.wealthwise.service;

import com.wealthwise.dto.ForecastResponse;
import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForecastService {

    @Autowired private TransactionRepository txRepo;
    @Autowired private UserRepository userRepo;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy");

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ForecastResponse getForecast() {
        User user = currentUser();
        List<Transaction> allTx = txRepo.findByUserOrderByDateDesc(user);

        // Running balance across all history
        double currentBalance = allTx.stream().mapToDouble(t -> {
            if (t.getType() == Transaction.TransactionType.INCOME)  return t.getAmount().doubleValue();
            if (t.getType() == Transaction.TransactionType.EXPENSE) return -t.getAmount().doubleValue();
            return 0;
        }).sum();

        // Build last-3-months historical data
        LocalDate today = LocalDate.now();
        List<ForecastResponse.MonthData> historical = new ArrayList<>();

        for (int i = 2; i >= 0; i--) {
            LocalDate monthStart = today.withDayOfMonth(1).minusMonths(i);
            LocalDate monthEnd   = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            double income     = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.INCOME);
            double expense    = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.EXPENSE);
            double saving     = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.SAVING);
            double investment = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.INVESTMENT);
            double balance    = balanceUpTo(allTx, monthEnd);

            historical.add(new ForecastResponse.MonthData(
                    monthStart.format(MONTH_FMT), income, expense, saving, investment, balance, false
            ));
        }

        // Average monthly net (income – expenses) over the 3 historical months
        double avgNet        = historical.stream().mapToDouble(m -> m.getIncome() - m.getExpense()).average().orElse(0);
        double avgIncome     = historical.stream().mapToDouble(ForecastResponse.MonthData::getIncome).average().orElse(0);
        double avgExpense    = historical.stream().mapToDouble(ForecastResponse.MonthData::getExpense).average().orElse(0);
        double avgSaving     = historical.stream().mapToDouble(ForecastResponse.MonthData::getSaving).average().orElse(0);
        double avgInvestment = historical.stream().mapToDouble(ForecastResponse.MonthData::getInvestment).average().orElse(0);

        // Project next 3 months
        List<ForecastResponse.MonthData> projected = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            LocalDate future = today.withDayOfMonth(1).plusMonths(i);
            double projBalance = currentBalance + (avgNet * i);
            projected.add(new ForecastResponse.MonthData(
                    future.format(MONTH_FMT), avgIncome, avgExpense, avgSaving, avgInvestment, projBalance, true
            ));
        }

        return new ForecastResponse(historical, projected, avgNet, currentBalance);
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

    private double balanceUpTo(List<Transaction> txs, LocalDate date) {
        return txs.stream()
                .filter(t -> !t.getDate().isAfter(date))
                .mapToDouble(t -> {
                    if (t.getType() == Transaction.TransactionType.INCOME)  return t.getAmount().doubleValue();
                    if (t.getType() == Transaction.TransactionType.EXPENSE) return -t.getAmount().doubleValue();
                    return 0;
                })
                .sum();
    }
}
