package com.wealthwise.service;

import com.wealthwise.dto.TransactionRequest;
import com.wealthwise.dto.TransactionResponse;
import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired private TransactionRepository txRepo;
    @Autowired private UserRepository userRepository;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
            t.getId(), t.getType(), t.getAmount(),
            t.getDescription(), t.getCategory(), t.getDate(), t.getCreatedAt()
        );
    }

    public List<TransactionResponse> getAllTransactions() {
        return txRepo.findByUserOrderByDateDesc(currentUser())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TransactionResponse createTransaction(TransactionRequest req) {
        Transaction tx = new Transaction();
        tx.setUser(currentUser());
        tx.setType(req.getType());
        tx.setAmount(req.getAmount());
        tx.setDescription(req.getDescription());
        tx.setCategory(req.getCategory());
        tx.setDate(req.getDate());
        return toResponse(txRepo.save(tx));
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest req) {
        Transaction tx = txRepo.findById(id)
                .filter(t -> t.getUser().getId().equals(currentUser().getId()))
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));
        tx.setType(req.getType());
        tx.setAmount(req.getAmount());
        tx.setDescription(req.getDescription());
        tx.setCategory(req.getCategory());
        tx.setDate(req.getDate());
        return toResponse(txRepo.save(tx));
    }

    public void deleteTransaction(Long id) {
        Transaction tx = txRepo.findById(id)
                .filter(t -> t.getUser().getId().equals(currentUser().getId()))
                .orElseThrow(() -> new RuntimeException("Transaction not found or access denied"));
        txRepo.delete(tx);
    }
}
