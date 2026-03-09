package com.wealthwise.service;

import com.wealthwise.dto.TransactionRequest;
import com.wealthwise.dto.TransactionResponse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository txRepo;
    @Mock private UserRepository userRepository;

    @InjectMocks private TransactionService transactionService;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = new User("alice", "hashed", "Alice Smith", "alice@test.com");
        user.setId(1L);

        otherUser = new User("bob", "hashed", "Bob Jones", "bob@test.com");
        otherUser.setId(2L);

        // Put alice in the security context
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, List.of())
        );

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
    }

    // ── getAllTransactions ────────────────────────────────────────────────────

    @Test
    void getAllTransactions_returnsOnlyUserTransactions() {
        Transaction tx = makeTransaction(1L, user, Transaction.TransactionType.INCOME, "100.00");
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(tx));

        List<TransactionResponse> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void getAllTransactions_returnsEmptyListWhenNoTransactions() {
        when(txRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of());

        List<TransactionResponse> result = transactionService.getAllTransactions();

        assertThat(result).isEmpty();
    }

    // ── createTransaction ────────────────────────────────────────────────────

    @Test
    void createTransaction_savesAndReturnsDto() {
        TransactionRequest req = makeRequest(Transaction.TransactionType.EXPENSE, "50.00", "Lunch", "Food");
        Transaction saved = makeTransaction(10L, user, Transaction.TransactionType.EXPENSE, "50.00");
        when(txRepo.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse result = transactionService.createTransaction(req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getType()).isEqualTo(Transaction.TransactionType.EXPENSE);
        assertThat(result.getAmount()).isEqualByComparingTo("50.00");
        verify(txRepo).save(any(Transaction.class));
    }

    // ── updateTransaction ────────────────────────────────────────────────────

    @Test
    void updateTransaction_updatesFieldsCorrectly() {
        Transaction existing = makeTransaction(5L, user, Transaction.TransactionType.INCOME, "200.00");
        when(txRepo.findById(5L)).thenReturn(Optional.of(existing));
        when(txRepo.save(existing)).thenReturn(existing);

        TransactionRequest req = makeRequest(Transaction.TransactionType.EXPENSE, "250.00", "Updated", "Bills");
        TransactionResponse result = transactionService.updateTransaction(5L, req);

        assertThat(result.getType()).isEqualTo(Transaction.TransactionType.EXPENSE);
        assertThat(result.getAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void updateTransaction_throwsWhenNotOwner() {
        Transaction otherTx = makeTransaction(5L, otherUser, Transaction.TransactionType.INCOME, "200.00");
        when(txRepo.findById(5L)).thenReturn(Optional.of(otherTx));

        TransactionRequest req = makeRequest(Transaction.TransactionType.INCOME, "200.00", "x", "y");
        assertThatThrownBy(() -> transactionService.updateTransaction(5L, req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("access denied");
    }

    // ── deleteTransaction ────────────────────────────────────────────────────

    @Test
    void deleteTransaction_callsRepositoryDelete() {
        Transaction tx = makeTransaction(7L, user, Transaction.TransactionType.EXPENSE, "30.00");
        when(txRepo.findById(7L)).thenReturn(Optional.of(tx));

        transactionService.deleteTransaction(7L);

        verify(txRepo).delete(tx);
    }

    @Test
    void deleteTransaction_throwsWhenNotOwner() {
        Transaction otherTx = makeTransaction(7L, otherUser, Transaction.TransactionType.EXPENSE, "30.00");
        when(txRepo.findById(7L)).thenReturn(Optional.of(otherTx));

        assertThatThrownBy(() -> transactionService.deleteTransaction(7L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("access denied");
        verify(txRepo, never()).delete(any());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Transaction makeTransaction(Long id, User owner,
                                        Transaction.TransactionType type, String amount) {
        Transaction tx = new Transaction();
        tx.setId(id);
        tx.setUser(owner);
        tx.setType(type);
        tx.setAmount(new BigDecimal(amount));
        tx.setDescription("desc");
        tx.setCategory("cat");
        tx.setDate(LocalDate.now());
        return tx;
    }

    private TransactionRequest makeRequest(Transaction.TransactionType type,
                                           String amount, String desc, String category) {
        TransactionRequest req = new TransactionRequest();
        req.setType(type);
        req.setAmount(new BigDecimal(amount));
        req.setDescription(desc);
        req.setCategory(category);
        req.setDate(LocalDate.now());
        return req;
    }
}
