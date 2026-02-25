package com.wealthwise.service;

import com.wealthwise.dto.GoalRequest;
import com.wealthwise.dto.GoalResponse;
import com.wealthwise.model.Goal;
import com.wealthwise.model.User;
import com.wealthwise.repository.GoalRepository;
import com.wealthwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    @Autowired private GoalRepository goalRepo;
    @Autowired private UserRepository userRepo;

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public List<GoalResponse> getAll() {
        return goalRepo.findByUserOrderByCreatedAtDesc(currentUser())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public GoalResponse create(GoalRequest req) {
        Goal g = new Goal();
        g.setUser(currentUser());
        applyRequest(g, req);
        return toResponse(goalRepo.save(g));
    }

    public GoalResponse update(Long id, GoalRequest req) {
        Goal g = ownedGoal(id);
        applyRequest(g, req);
        return toResponse(goalRepo.save(g));
    }

    /** Add a lump-sum deposit on top of the existing savedAmount. */
    public GoalResponse deposit(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Deposit amount must be positive");
        Goal g = ownedGoal(id);
        BigDecimal newSaved = g.getSavedAmount().add(amount);
        // Cap at targetAmount
        if (newSaved.compareTo(g.getTargetAmount()) > 0) newSaved = g.getTargetAmount();
        g.setSavedAmount(newSaved);
        return toResponse(goalRepo.save(g));
    }

    public void delete(Long id) {
        goalRepo.delete(ownedGoal(id));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Goal ownedGoal(Long id) {
        return goalRepo.findById(id)
                .filter(g -> g.getUser().getId().equals(currentUser().getId()))
                .orElseThrow(() -> new RuntimeException("Goal not found or access denied"));
    }

    private void applyRequest(Goal g, GoalRequest req) {
        if (req.getName()               != null) g.setName(req.getName());
        if (req.getTargetAmount()       != null) g.setTargetAmount(req.getTargetAmount());
        if (req.getMonthlyContribution()!= null) g.setMonthlyContribution(req.getMonthlyContribution());
        if (req.getIcon()               != null) g.setIcon(req.getIcon());
        g.setDeadline(req.getDeadline()); // nullable intentionally

        BigDecimal saved = req.getSavedAmount();
        if (saved != null) {
            if (saved.compareTo(BigDecimal.ZERO) < 0) saved = BigDecimal.ZERO;
            if (g.getTargetAmount() != null && saved.compareTo(g.getTargetAmount()) > 0)
                saved = g.getTargetAmount();
            g.setSavedAmount(saved);
        } else if (g.getSavedAmount() == null) {
            g.setSavedAmount(BigDecimal.ZERO);
        }
    }

    private GoalResponse toResponse(Goal g) {
        GoalResponse r = new GoalResponse();
        r.setId(g.getId());
        r.setName(g.getName());
        r.setTargetAmount(g.getTargetAmount());
        r.setSavedAmount(g.getSavedAmount());
        r.setMonthlyContribution(g.getMonthlyContribution());
        r.setDeadline(g.getDeadline());
        r.setIcon(g.getIcon());
        r.setCreatedAt(g.getCreatedAt());

        BigDecimal target  = g.getTargetAmount();
        BigDecimal saved   = g.getSavedAmount() != null ? g.getSavedAmount() : BigDecimal.ZERO;
        BigDecimal monthly = g.getMonthlyContribution();

        BigDecimal remaining = target.subtract(saved);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
        r.setRemainingAmount(remaining);

        // Progress %
        double pct = target.compareTo(BigDecimal.ZERO) > 0
                ? saved.doubleValue() / target.doubleValue() * 100.0
                : 0;
        r.setProgressPercent(Math.min(pct, 100.0));

        // Estimated completion date based on monthly contribution
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            // Already reached
            r.setMonthsToCompletion(0);
            r.setEstimatedCompletionDate(LocalDate.now());
        } else if (monthly != null && monthly.compareTo(BigDecimal.ZERO) > 0) {
            int months = remaining.divide(monthly, 0, RoundingMode.CEILING).intValue();
            r.setMonthsToCompletion(months);
            r.setEstimatedCompletionDate(LocalDate.now().plusMonths(months));
        }
        // else: no contribution set → completion date stays null

        return r;
    }
}
