package com.wealthwise.service;

import com.wealthwise.model.Goal;
import com.wealthwise.model.Notification;
import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import com.wealthwise.repository.GoalRepository;
import com.wealthwise.repository.NotificationRepository;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock private NotificationRepository notifRepo;
    @Mock private TransactionRepository  txRepo;
    @Mock private GoalRepository         goalRepo;
    @Mock private UserRepository         userRepo;

    @InjectMocks private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("alice", "hashed", "Alice", "alice@test.com");
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, List.of())
        );
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(user));

        // Default: no existing notifications
        when(notifRepo.existsByUserAndRefKey(eq(user), any())).thenReturn(false);
        when(notifRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(goalRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
    }

    // ── INCOME_DETECTED ───────────────────────────────────────────────────────

    @Test
    void getAll_createsIncomeDetectedWhenMonthlyIncomeIsPositive() {
        Transaction income = makeTx(Transaction.TransactionType.INCOME, "3000", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(income));

        notificationService.getAll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
            .anyMatch(n -> n.getType() == Notification.NotificationType.INCOME_DETECTED);
    }

    @Test
    void getAll_doesNotCreateIncomeDetectedWhenNoIncome() {
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of());

        notificationService.getAll();

        verify(notifRepo, never()).save(any());
    }

    // ── BUDGET_EXCEEDED ───────────────────────────────────────────────────────

    @Test
    void getAll_createsBudgetDangerWhenExpensesExceedIncome() {
        Transaction income  = makeTx(Transaction.TransactionType.INCOME,  "1000", LocalDate.now());
        Transaction expense = makeTx(Transaction.TransactionType.EXPENSE, "1500", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(income, expense));

        notificationService.getAll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
            .anyMatch(n -> n.getType() == Notification.NotificationType.BUDGET_EXCEEDED
                       && n.getTitle().contains("exceeds income"));
    }

    @Test
    void getAll_createsBudgetExceededWhenBurnRateAbove80Percent() {
        Transaction income  = makeTx(Transaction.TransactionType.INCOME,  "1000", LocalDate.now());
        Transaction expense = makeTx(Transaction.TransactionType.EXPENSE,  "850", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(income, expense));

        notificationService.getAll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
            .anyMatch(n -> n.getType() == Notification.NotificationType.BUDGET_EXCEEDED
                       && n.getTitle().contains("threshold"));
    }

    @Test
    void getAll_createsBudgetNoIncomeWhenExpensesWithoutIncome() {
        Transaction expense = makeTx(Transaction.TransactionType.EXPENSE, "500", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(expense));

        notificationService.getAll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
            .anyMatch(n -> n.getType() == Notification.NotificationType.BUDGET_EXCEEDED
                       && n.getTitle().contains("no income"));
    }

    // ── GOAL_REACHED ──────────────────────────────────────────────────────────

    @Test
    void getAll_createsGoalReachedWhenSavedEqualsTarget() {
        Goal g = makeGoal(5L, "Vacation", "1000", "1000");
        when(goalRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(g));
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of());

        notificationService.getAll();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifRepo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues())
            .anyMatch(n -> n.getType() == Notification.NotificationType.GOAL_REACHED);
    }

    @Test
    void getAll_doesNotCreateGoalReachedWhenNotFunded() {
        Goal g = makeGoal(5L, "Vacation", "1000", "500");
        when(goalRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(g));
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of());

        notificationService.getAll();

        verify(notifRepo, never()).save(any());
    }

    // ── Deduplication ─────────────────────────────────────────────────────────

    @Test
    void getAll_doesNotDuplicateExistingNotifications() {
        Transaction income = makeTx(Transaction.TransactionType.INCOME, "3000", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(income));
        // Simulate notification already exists
        when(notifRepo.existsByUserAndRefKey(eq(user), any())).thenReturn(true);

        notificationService.getAll();

        verify(notifRepo, never()).save(any());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Transaction makeTx(Transaction.TransactionType type, String amount, LocalDate date) {
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setType(type);
        tx.setAmount(new BigDecimal(amount));
        tx.setDescription("desc");
        tx.setCategory("cat");
        tx.setDate(date);
        return tx;
    }

    private Goal makeGoal(Long id, String name, String target, String saved) {
        Goal g = new Goal();
        g.setId(id);
        g.setUser(user);
        g.setName(name);
        g.setTargetAmount(new BigDecimal(target));
        g.setSavedAmount(new BigDecimal(saved));
        g.setMonthlyContribution(BigDecimal.valueOf(100));
        return g;
    }
}
