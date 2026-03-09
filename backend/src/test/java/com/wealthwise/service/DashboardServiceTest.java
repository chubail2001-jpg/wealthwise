package com.wealthwise.service;

import com.wealthwise.dto.DashboardResponse;
import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private TransactionRepository txRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks private DashboardService dashboardService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("alice", "hashed", "Alice", "alice@test.com");
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, List.of())
        );
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(user));
    }

    @Test
    void getSummary_returnsZeroWhenNoTransactions() {
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of());

        DashboardResponse result = dashboardService.getSummary();

        assertThat(result.getMonthIncome()).isZero();
        assertThat(result.getMonthExpense()).isZero();
        assertThat(result.getMonthSaving()).isZero();
        assertThat(result.getMonthInvestment()).isZero();
        assertThat(result.getTotalBalance()).isZero();
    }

    @Test
    void getSummary_sumsIncomeCorrectly() {
        Transaction t1 = makeTx(Transaction.TransactionType.INCOME, "3000", LocalDate.now());
        Transaction t2 = makeTx(Transaction.TransactionType.INCOME, "500",  LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(t1, t2));

        DashboardResponse result = dashboardService.getSummary();

        assertThat(result.getMonthIncome()).isEqualTo(3500.0);
    }

    @Test
    void getSummary_sumsExpenseCorrectly() {
        Transaction t = makeTx(Transaction.TransactionType.EXPENSE, "800", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(t));

        DashboardResponse result = dashboardService.getSummary();

        assertThat(result.getMonthExpense()).isEqualTo(800.0);
    }

    @Test
    void getSummary_sumsSavingCorrectly() {
        Transaction t = makeTx(Transaction.TransactionType.SAVING, "200", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(t));

        DashboardResponse result = dashboardService.getSummary();

        assertThat(result.getMonthSaving()).isEqualTo(200.0);
    }

    @Test
    void getSummary_sumsInvestmentCorrectly() {
        Transaction t = makeTx(Transaction.TransactionType.INVESTMENT, "400", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(t));

        DashboardResponse result = dashboardService.getSummary();

        assertThat(result.getMonthInvestment()).isEqualTo(400.0);
    }

    @Test
    void getSummary_totalBalanceIsIncomeMinusExpense() {
        Transaction income  = makeTx(Transaction.TransactionType.INCOME,  "3000", LocalDate.now());
        Transaction expense = makeTx(Transaction.TransactionType.EXPENSE, "1200", LocalDate.now());
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(income, expense));

        DashboardResponse result = dashboardService.getSummary();

        assertThat(result.getTotalBalance()).isEqualTo(1800.0);
    }

    @Test
    void getSummary_excludesTransactionsFromPreviousMonth() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        Transaction old = makeTx(Transaction.TransactionType.INCOME, "5000", lastMonth);
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(old));

        DashboardResponse result = dashboardService.getSummary();

        // Old income counts toward totalBalance but NOT toward monthIncome
        assertThat(result.getMonthIncome()).isZero();
        assertThat(result.getTotalBalance()).isEqualTo(5000.0);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

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
}
