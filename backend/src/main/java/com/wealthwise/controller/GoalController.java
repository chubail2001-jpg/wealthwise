package com.wealthwise.controller;

import com.wealthwise.dto.GoalRequest;
import com.wealthwise.dto.GoalResponse;
import com.wealthwise.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired private GoalService goalService;

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getAll() {
        return ResponseEntity.ok(goalService.getAll());
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(@RequestBody GoalRequest req) {
        return ResponseEntity.ok(goalService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(@PathVariable Long id,
                                               @RequestBody GoalRequest req) {
        return ResponseEntity.ok(goalService.update(id, req));
    }

    /** Add a deposit (lump-sum) to a goal's savedAmount */
    @PatchMapping("/{id}/deposit")
    public ResponseEntity<GoalResponse> deposit(@PathVariable Long id,
                                                @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.get("amount");
        return ResponseEntity.ok(goalService.deposit(id, amount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        goalService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Goal deleted"));
    }
}
