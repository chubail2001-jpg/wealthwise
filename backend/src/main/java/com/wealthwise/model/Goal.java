package com.wealthwise.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    /** Final savings target */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    /** Amount saved so far toward this goal */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal savedAmount = BigDecimal.ZERO;

    /** How much the user plans to contribute each month */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyContribution;

    /** Optional hard deadline the user sets */
    private LocalDate deadline;

    /** Emoji / short icon string, e.g. "🚗" or "car" */
    private String icon;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Goal() {}

    @PrePersist
    protected void onCreate() {
        if (createdAt  == null) createdAt  = LocalDateTime.now();
        if (savedAmount == null) savedAmount = BigDecimal.ZERO;
    }

    // --- Getters ---
    public Long          getId()                  { return id; }
    public User          getUser()                { return user; }
    public String        getName()                { return name; }
    public BigDecimal    getTargetAmount()        { return targetAmount; }
    public BigDecimal    getSavedAmount()         { return savedAmount; }
    public BigDecimal    getMonthlyContribution() { return monthlyContribution; }
    public LocalDate     getDeadline()            { return deadline; }
    public String        getIcon()                { return icon; }
    public LocalDateTime getCreatedAt()           { return createdAt; }

    // --- Setters ---
    public void setId(Long id)                                  { this.id = id; }
    public void setUser(User user)                              { this.user = user; }
    public void setName(String name)                            { this.name = name; }
    public void setTargetAmount(BigDecimal targetAmount)        { this.targetAmount = targetAmount; }
    public void setSavedAmount(BigDecimal savedAmount)          { this.savedAmount = savedAmount; }
    public void setMonthlyContribution(BigDecimal v)            { this.monthlyContribution = v; }
    public void setDeadline(LocalDate deadline)                 { this.deadline = deadline; }
    public void setIcon(String icon)                            { this.icon = icon; }
}
