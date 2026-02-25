package com.wealthwise.service;

import com.wealthwise.dto.NotificationResponse;
import com.wealthwise.model.Goal;
import com.wealthwise.model.Notification;
import com.wealthwise.model.Notification.NotificationType;
import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import com.wealthwise.repository.GoalRepository;
import com.wealthwise.repository.NotificationRepository;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notifRepo;
    @Autowired private TransactionRepository  txRepo;
    @Autowired private GoalRepository         goalRepo;
    @Autowired private UserRepository         userRepo;

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    // -------------------------------------------------------------------------
    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Triggers generation, then returns all notifications newest-first. */
    public List<NotificationResponse> getAll() {
        User user = currentUser();
        generateForUser(user);
        return notifRepo.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public long getUnreadCount() {
        return notifRepo.countByUserAndReadFalse(currentUser());
    }

    public NotificationResponse markRead(Long id) {
        Notification n = notifRepo.findById(id)
                .filter(x -> x.getUser().getId().equals(currentUser().getId()))
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        return toResponse(notifRepo.save(n));
    }

    public void markAllRead() {
        notifRepo.markAllReadByUser(currentUser());
    }

    // -------------------------------------------------------------------------
    // Auto-generation
    // -------------------------------------------------------------------------

    private void generateForUser(User user) {
        LocalDate today      = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd   = today.withDayOfMonth(today.lengthOfMonth());
        String    monthKey   = today.format(MONTH_KEY);

        List<Transaction> allTx = txRepo.findByUserOrderByDateDesc(user);

        double monthIncome  = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.INCOME);
        double monthExpense = sumType(allTx, monthStart, monthEnd, Transaction.TransactionType.EXPENSE);

        // ── 1. INCOME DETECTED ────────────────────────────────────────────────
        if (monthIncome > 0) {
            String refKey = "INCOME_DETECTED-" + monthKey;
            if (!notifRepo.existsByUserAndRefKey(user, refKey)) {
                save(user, NotificationType.INCOME_DETECTED, refKey,
                        "Income detected this month",
                        String.format("$%.0f in income recorded for %s. Great start — keep tracking your finances!",
                                monthIncome, today.getMonth().name().charAt(0)
                                        + today.getMonth().name().substring(1).toLowerCase()
                                        + " " + today.getYear()));
            }
        }

        // ── 2. BUDGET EXCEEDED ────────────────────────────────────────────────
        if (monthIncome > 0 && monthExpense > 0) {
            double burnRate = (monthExpense / monthIncome) * 100;

            if (burnRate > 100) {
                String refKey = "BUDGET_DANGER-" + monthKey;
                if (!notifRepo.existsByUserAndRefKey(user, refKey)) {
                    save(user, NotificationType.BUDGET_EXCEEDED, refKey,
                            "Spending exceeds income!",
                            String.format("You've spent $%.0f but only earned $%.0f this month (%.0f%% burn rate). " +
                                    "Immediate action needed to avoid debt.", monthExpense, monthIncome, burnRate));
                }
            } else if (burnRate > 80) {
                String refKey = "BUDGET_EXCEEDED-" + monthKey;
                if (!notifRepo.existsByUserAndRefKey(user, refKey)) {
                    save(user, NotificationType.BUDGET_EXCEEDED, refKey,
                            "Budget threshold exceeded",
                            String.format("Your expenses ($%.0f) have consumed %.0f%% of your income this month. " +
                                    "Consider reducing discretionary spending.", monthExpense, burnRate));
                }
            }
        } else if (monthExpense > 0 && monthIncome == 0) {
            String refKey = "BUDGET_NO_INCOME-" + monthKey;
            if (!notifRepo.existsByUserAndRefKey(user, refKey)) {
                save(user, NotificationType.BUDGET_EXCEEDED, refKey,
                        "Expenses with no income recorded",
                        String.format("You have $%.0f in expenses this month but no income logged yet. " +
                                "Make sure to record all your income sources.", monthExpense));
            }
        }

        // ── 3. GOAL REACHED ───────────────────────────────────────────────────
        List<Goal> goals = goalRepo.findByUserOrderByCreatedAtDesc(user);
        for (Goal g : goals) {
            if (g.getTargetAmount() == null || g.getSavedAmount() == null) continue;
            double pct = g.getSavedAmount().doubleValue() / g.getTargetAmount().doubleValue() * 100;
            if (pct >= 100) {
                String refKey = "GOAL_REACHED-" + g.getId();
                if (!notifRepo.existsByUserAndRefKey(user, refKey)) {
                    save(user, NotificationType.GOAL_REACHED, refKey,
                            "Goal reached: " + g.getName() + "!",
                            String.format("Congratulations! You've fully funded your \"%s\" goal " +
                                    "with $%.0f saved. Time to celebrate and set your next goal!",
                                    g.getName(), g.getSavedAmount().doubleValue()));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    private void save(User user, NotificationType type, String refKey, String title, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setRefKey(refKey);
        n.setTitle(title);
        n.setMessage(message);
        notifRepo.save(n);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType().name(), n.getTitle(),
                n.getMessage(), n.isRead(), n.getCreatedAt());
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
