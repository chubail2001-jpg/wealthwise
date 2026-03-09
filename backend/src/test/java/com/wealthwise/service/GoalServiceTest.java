package com.wealthwise.service;

import com.wealthwise.dto.GoalRequest;
import com.wealthwise.dto.GoalResponse;
import com.wealthwise.model.Goal;
import com.wealthwise.model.User;
import com.wealthwise.repository.GoalRepository;
import com.wealthwise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoalServiceTest {

    @Mock private GoalRepository goalRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks private GoalService goalService;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = new User("alice", "hashed", "Alice", "alice@test.com");
        user.setId(1L);

        otherUser = new User("bob", "hashed", "Bob", "bob@test.com");
        otherUser.setId(2L);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, List.of())
        );
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(user));
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsGoalsForCurrentUser() {
        Goal g = makeGoal(1L, user, "Emergency Fund", "1000", "200", "100");
        when(goalRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(g));

        List<GoalResponse> result = goalService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Emergency Fund");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_savedAmountDefaultsToZero() {
        GoalRequest req = makeRequest("Vacation", "2000", "150", null);
        Goal saved = makeGoal(10L, user, "Vacation", "2000", "0", "150");
        when(goalRepo.save(any(Goal.class))).thenReturn(saved);

        GoalResponse result = goalService.create(req);

        assertThat(result.getSavedAmount()).isEqualByComparingTo("0");
    }

    @Test
    void create_returnsResponseWithCorrectName() {
        GoalRequest req = makeRequest("Car", "5000", "300", null);
        Goal saved = makeGoal(11L, user, "Car", "5000", "0", "300");
        when(goalRepo.save(any(Goal.class))).thenReturn(saved);

        GoalResponse result = goalService.create(req);

        assertThat(result.getName()).isEqualTo("Car");
        verify(goalRepo).save(any(Goal.class));
    }

    // ── deposit ───────────────────────────────────────────────────────────────

    @Test
    void deposit_addsAmountToSavedAmount() {
        Goal g = makeGoal(1L, user, "Laptop", "1000", "200", "100");
        when(goalRepo.findById(1L)).thenReturn(Optional.of(g));
        when(goalRepo.save(g)).thenReturn(g);

        GoalResponse result = goalService.deposit(1L, new BigDecimal("300"));

        assertThat(result.getSavedAmount()).isEqualByComparingTo("500");
    }

    @Test
    void deposit_capsAtTargetAmount() {
        Goal g = makeGoal(1L, user, "Laptop", "1000", "900", "100");
        when(goalRepo.findById(1L)).thenReturn(Optional.of(g));
        when(goalRepo.save(g)).thenReturn(g);

        GoalResponse result = goalService.deposit(1L, new BigDecimal("500"));

        // 900 + 500 = 1400, should be capped at 1000
        assertThat(result.getSavedAmount()).isEqualByComparingTo("1000");
    }

    @Test
    void deposit_throwsForZeroAmount() {
        assertThatThrownBy(() -> goalService.deposit(1L, BigDecimal.ZERO))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("positive");
    }

    @Test
    void deposit_throwsForNegativeAmount() {
        assertThatThrownBy(() -> goalService.deposit(1L, new BigDecimal("-50")))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("positive");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_callsRepositoryDelete() {
        Goal g = makeGoal(3L, user, "Trip", "500", "0", "50");
        when(goalRepo.findById(3L)).thenReturn(Optional.of(g));

        goalService.delete(3L);

        verify(goalRepo).delete(g);
    }

    @Test
    void delete_throwsWhenNotOwner() {
        Goal otherGoal = makeGoal(3L, otherUser, "Trip", "500", "0", "50");
        when(goalRepo.findById(3L)).thenReturn(Optional.of(otherGoal));

        assertThatThrownBy(() -> goalService.delete(3L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("access denied");
        verify(goalRepo, never()).delete(any());
    }

    // ── toResponse calculations ───────────────────────────────────────────────

    @Test
    void progressPercent_isCalculatedCorrectly() {
        Goal g = makeGoal(1L, user, "Fund", "1000", "250", "100");
        when(goalRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(g));

        GoalResponse r = goalService.getAll().get(0);

        assertThat(r.getProgressPercent()).isEqualTo(25.0);
    }

    @Test
    void progressPercent_capsAt100WhenOverfunded() {
        Goal g = makeGoal(1L, user, "Fund", "1000", "1000", "100");
        when(goalRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(g));

        GoalResponse r = goalService.getAll().get(0);

        assertThat(r.getProgressPercent()).isEqualTo(100.0);
    }

    @Test
    void remainingAmount_isZeroWhenGoalReached() {
        Goal g = makeGoal(1L, user, "Fund", "1000", "1000", "100");
        when(goalRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(g));

        GoalResponse r = goalService.getAll().get(0);

        assertThat(r.getRemainingAmount()).isEqualByComparingTo("0");
    }

    @Test
    void monthsToCompletion_isCalculatedCorrectly() {
        // remaining = 500, monthly = 100 → 5 months
        Goal g = makeGoal(1L, user, "Fund", "1000", "500", "100");
        when(goalRepo.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(g));

        GoalResponse r = goalService.getAll().get(0);

        assertThat(r.getMonthsToCompletion()).isEqualTo(5);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Goal makeGoal(Long id, User owner, String name,
                          String target, String saved, String monthly) {
        Goal g = new Goal();
        g.setId(id);
        g.setUser(owner);
        g.setName(name);
        g.setTargetAmount(new BigDecimal(target));
        g.setSavedAmount(new BigDecimal(saved));
        g.setMonthlyContribution(new BigDecimal(monthly));
        g.setDeadline(LocalDate.now().plusMonths(12));
        return g;
    }

    private GoalRequest makeRequest(String name, String target, String monthly, String saved) {
        GoalRequest req = new GoalRequest();
        req.setName(name);
        req.setTargetAmount(new BigDecimal(target));
        req.setMonthlyContribution(new BigDecimal(monthly));
        if (saved != null) req.setSavedAmount(new BigDecimal(saved));
        req.setDeadline(LocalDate.now().plusMonths(12));
        return req;
    }
}
