package com.wealthwise.controller;

import com.wealthwise.dto.TransactionRequest;
import com.wealthwise.dto.TransactionResponse;
import com.wealthwise.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired private TransactionService txService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(txService.getAllTransactions());
    }

    // GlobalExceptionHandler catches any RuntimeException thrown by the service
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody TransactionRequest req) {
        return ResponseEntity.ok(txService.createTransaction(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id,
                                                      @RequestBody TransactionRequest req) {
        return ResponseEntity.ok(txService.updateTransaction(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        txService.deleteTransaction(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }
}
