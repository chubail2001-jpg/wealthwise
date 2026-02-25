package com.wealthwise.service;

import com.wealthwise.dto.InsightResponse;
import com.wealthwise.dto.InsightResponse.Insight;
import com.wealthwise.model.Transaction;
import com.wealthwise.model.Transaction.TransactionType;
import com.wealthwise.model.User;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightService {

    @Autowired private TransactionRepository txRepo;
    @Autowired private UserRepository userRepo;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public InsightResponse getInsights() {
        List<Transaction> all = txRepo.findByUserOrderByDateDesc(currentUser());
        List<Insight> insights = new ArrayList<>();

        LocalDate today     = LocalDate.now();
        LocalDate thisStart = today.withDayOfMonth(1);
        LocalDate thisEnd   = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate lastStart = thisStart.minusMonths(1);
        LocalDate lastEnd   = thisStart.minusDays(1);

        // --- per-month totals -------------------------------------------------
        double thisIncome     = sum(all, thisStart, thisEnd, TransactionType.INCOME);
        double thisExpense    = sum(all, thisStart, thisEnd, TransactionType.EXPENSE);
        double thisSaving     = sum(all, thisStart, thisEnd, TransactionType.SAVING);
        double thisInvestment = sum(all, thisStart, thisEnd, TransactionType.INVESTMENT);

        double lastIncome     = sum(all, lastStart, lastEnd, TransactionType.INCOME);
        double lastExpense    = sum(all, lastStart, lastEnd, TransactionType.EXPENSE);
        double lastSaving     = sum(all, lastStart, lastEnd, TransactionType.SAVING);

        // ======================================================================
        // 1. EXPENSE TREND — month-over-month
        // ======================================================================
        if (lastExpense > 0) {
            double expenseDelta = ((thisExpense - lastExpense) / lastExpense) * 100;
            if (expenseDelta >= 15) {
                insights.add(new Insight("WARNING",
                        "Expenses spiked this month",
                        String.format("Your expenses increased by %.0f%% compared to last month. " +
                                "Review your recent spending to identify the cause.", expenseDelta),
                        expenseDelta));
            } else if (expenseDelta <= -10) {
                insights.add(new Insight("POSITIVE",
                        "Expenses down this month",
                        String.format("Great job! Your expenses dropped by %.0f%% compared to last month.",
                                Math.abs(expenseDelta)),
                        expenseDelta));
            } else {
                insights.add(new Insight("INFO",
                        "Expenses stable",
                        "Your spending is consistent with last month. Keep it up!",
                        expenseDelta));
            }
        }

        // ======================================================================
        // 2. INCOME TREND — month-over-month
        // ======================================================================
        if (lastIncome > 0 && thisIncome > 0) {
            double incomeDelta = ((thisIncome - lastIncome) / lastIncome) * 100;
            if (incomeDelta >= 10) {
                insights.add(new Insight("POSITIVE",
                        "Income increased",
                        String.format("Your income grew by %.0f%% this month — great momentum!", incomeDelta),
                        incomeDelta));
            } else if (incomeDelta <= -10) {
                insights.add(new Insight("WARNING",
                        "Income declined this month",
                        String.format("Your income dropped by %.0f%% compared to last month. " +
                                "Consider diversifying your income sources.", Math.abs(incomeDelta)),
                        incomeDelta));
            }
        }

        // ======================================================================
        // 3. SPENDING-TO-INCOME RATIO (burn rate)
        // ======================================================================
        if (thisIncome > 0) {
            double burnRate = (thisExpense / thisIncome) * 100;
            if (burnRate > 100) {
                insights.add(new Insight("DANGER",
                        "Spending exceeds income!",
                        String.format("You're spending %.0f%% of your income this month. " +
                                "Reduce expenses immediately to avoid going into debt.", burnRate),
                        burnRate));
            } else if (burnRate > 80) {
                insights.add(new Insight("WARNING",
                        "High burn rate",
                        String.format("Expenses are consuming %.0f%% of your income. " +
                                "Try to keep this below 70%%.", burnRate),
                        burnRate));
            }
        } else if (thisExpense > 0) {
            insights.add(new Insight("WARNING",
                    "No income recorded this month",
                    "You have expenses but no income logged this month. Make sure your income is tracked.",
                    null));
        }

        // ======================================================================
        // 4. SAVINGS RATE — this month
        // ======================================================================
        if (thisIncome > 0) {
            double savingsRate = (thisSaving / thisIncome) * 100;
            if (savingsRate >= 20) {
                insights.add(new Insight("POSITIVE",
                        "Strong savings rate",
                        String.format("You're saving %.0f%% of your income this month — excellent financial discipline!",
                                savingsRate),
                        savingsRate));
            } else if (savingsRate >= 10) {
                insights.add(new Insight("INFO",
                        "Moderate savings rate",
                        String.format("Your savings rate is %.0f%%. Aim for 20%% or more for long-term security.",
                                savingsRate),
                        savingsRate));
            } else if (thisIncome > 0) {
                insights.add(new Insight("WARNING",
                        "Low savings rate",
                        String.format("Only %.0f%% of your income is being saved. " +
                                "Try to automate savings to hit the 20%% target.", savingsRate),
                        savingsRate));
            }
        }

        // ======================================================================
        // 5. SAVINGS RATE CHANGE — improved or declined vs last month
        // ======================================================================
        if (thisIncome > 0 && lastIncome > 0) {
            double thisRate = thisSaving / thisIncome;
            double lastRate = lastSaving / lastIncome;
            if (thisRate > lastRate + 0.05) {
                insights.add(new Insight("POSITIVE",
                        "Savings rate improved",
                        String.format("Your savings rate rose from %.0f%% to %.0f%% — you're building better habits!",
                                lastRate * 100, thisRate * 100),
                        (thisRate - lastRate) * 100));
            } else if (thisRate < lastRate - 0.05) {
                insights.add(new Insight("WARNING",
                        "Savings rate declined",
                        String.format("Your savings rate dropped from %.0f%% to %.0f%% this month. " +
                                "Try to cut discretionary spending.", lastRate * 100, thisRate * 100),
                        (thisRate - lastRate) * 100));
            }
        }

        // ======================================================================
        // 6. INVESTMENT ACTIVITY
        // ======================================================================
        if (thisInvestment > 0) {
            double investRate = thisIncome > 0 ? (thisInvestment / thisIncome) * 100 : 0;
            insights.add(new Insight("POSITIVE",
                    "You're investing this month",
                    String.format("$%.0f invested — that's %.0f%% of your income working for your future.",
                            thisInvestment, investRate),
                    thisInvestment));
        } else {
            insights.add(new Insight("INFO",
                    "No investments this month",
                    "Consider allocating even a small portion of income to investments for long-term growth.",
                    null));
        }

        // ======================================================================
        // 7. CATEGORY ANOMALY DETECTION — "X spending unusually high"
        //    Compare this month per-category expense vs 3-month average
        // ======================================================================
        // Build: category → list of monthly totals over the past 3 months
        Map<String, List<Double>> categoryMonthly = new LinkedHashMap<>();

        for (int i = 1; i <= 3; i++) {
            LocalDate mStart = thisStart.minusMonths(i);
            LocalDate mEnd   = mStart.withDayOfMonth(mStart.lengthOfMonth());

            Map<String, Double> monthCats = all.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE
                            && !t.getDate().isBefore(mStart)
                            && !t.getDate().isAfter(mEnd)
                            && t.getCategory() != null)
                    .collect(Collectors.groupingBy(
                            Transaction::getCategory,
                            Collectors.summingDouble(t -> t.getAmount().doubleValue())));

            monthCats.forEach((cat, amt) ->
                    categoryMonthly.computeIfAbsent(cat, k -> new ArrayList<>()).add(amt));
        }

        // This month's expense per category
        Map<String, Double> thisCats = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE
                        && !t.getDate().isBefore(thisStart)
                        && !t.getDate().isAfter(thisEnd)
                        && t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())));

        for (Map.Entry<String, Double> entry : thisCats.entrySet()) {
            String cat       = entry.getKey();
            double thisAmt   = entry.getValue();
            List<Double> history = categoryMonthly.getOrDefault(cat, Collections.emptyList());

            if (history.isEmpty()) continue;

            double avgAmt = history.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            if (avgAmt == 0) continue;

            double overPct = ((thisAmt - avgAmt) / avgAmt) * 100;

            if (overPct >= 50) {
                insights.add(new Insight("WARNING",
                        cat + " spending unusually high",
                        String.format("You spent $%.0f on %s this month — %.0f%% above your 3-month average of $%.0f.",
                                thisAmt, cat.toLowerCase(), overPct, avgAmt),
                        overPct));
            } else if (overPct <= -30) {
                insights.add(new Insight("POSITIVE",
                        cat + " spending reduced",
                        String.format("Your %s spending is $%.0f — %.0f%% below your usual average. Nice restraint!",
                                cat.toLowerCase(), thisAmt, Math.abs(overPct)),
                        overPct));
            }
        }

        // ======================================================================
        // 8. NET WORTH TRAJECTORY
        // ======================================================================
        double totalBalance = all.stream().mapToDouble(t -> {
            if (t.getType() == TransactionType.INCOME)  return t.getAmount().doubleValue();
            if (t.getType() == TransactionType.EXPENSE) return -t.getAmount().doubleValue();
            return 0;
        }).sum();

        double lastMonthBalance = all.stream()
                .filter(t -> !t.getDate().isAfter(lastEnd))
                .mapToDouble(t -> {
                    if (t.getType() == TransactionType.INCOME)  return t.getAmount().doubleValue();
                    if (t.getType() == TransactionType.EXPENSE) return -t.getAmount().doubleValue();
                    return 0;
                }).sum();

        if (lastMonthBalance > 0) {
            double netGrowth = totalBalance - lastMonthBalance;
            if (netGrowth > 0) {
                insights.add(new Insight("POSITIVE",
                        "Net worth growing",
                        String.format("Your balance grew by $%.0f this month. Keep this positive trajectory!", netGrowth),
                        netGrowth));
            } else if (netGrowth < 0) {
                insights.add(new Insight("WARNING",
                        "Net worth declined",
                        String.format("Your balance dropped by $%.0f this month. Review your expenses.", Math.abs(netGrowth)),
                        netGrowth));
            }
        }

        return new InsightResponse(insights);
    }

    // -------------------------------------------------------------------------
    private double sum(List<Transaction> txs, LocalDate start, LocalDate end, TransactionType type) {
        return txs.stream()
                .filter(t -> t.getType() == type
                        && !t.getDate().isBefore(start)
                        && !t.getDate().isAfter(end))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }
}
