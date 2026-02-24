package com.wealthwise.repository;

import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserOrderByDateDesc(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.type = :type ORDER BY t.date DESC")
    List<Transaction> findByUserAndType(User user, Transaction.TransactionType type);
}
