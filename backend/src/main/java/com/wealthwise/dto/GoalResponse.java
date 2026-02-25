package com.wealthwise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalResponse {

    private Long          id;
    private String        name;
    private BigDecimal    targetAmount;
    private BigDecimal    savedAmount;
    private BigDecimal    remainingAmount;
    private BigDecimal    monthlyContribution;
    private LocalDate     deadline;
    private String        icon;
    private double        progressPercent;
    /** How many months until goal is fully funded at current contribution rate. Null if already reached. */
    private Integer       monthsToCompletion;
    /** Estimated calendar date of completion based on monthly contribution. */
    private LocalDate     estimatedCompletionDate;
    private LocalDateTime createdAt;

    public GoalResponse() {}

    // --- Getters ---
    public Long          getId()                     { return id; }
    public String        getName()                   { return name; }
    public BigDecimal    getTargetAmount()           { return targetAmount; }
    public BigDecimal    getSavedAmount()            { return savedAmount; }
    public BigDecimal    getRemainingAmount()        { return remainingAmount; }
    public BigDecimal    getMonthlyContribution()    { return monthlyContribution; }
    public LocalDate     getDeadline()               { return deadline; }
    public String        getIcon()                   { return icon; }
    public double        getProgressPercent()        { return progressPercent; }
    public Integer       getMonthsToCompletion()     { return monthsToCompletion; }
    public LocalDate     getEstimatedCompletionDate(){ return estimatedCompletionDate; }
    public LocalDateTime getCreatedAt()              { return createdAt; }

    // --- Setters ---
    public void setId(Long v)                              { id = v; }
    public void setName(String v)                          { name = v; }
    public void setTargetAmount(BigDecimal v)              { targetAmount = v; }
    public void setSavedAmount(BigDecimal v)               { savedAmount = v; }
    public void setRemainingAmount(BigDecimal v)           { remainingAmount = v; }
    public void setMonthlyContribution(BigDecimal v)       { monthlyContribution = v; }
    public void setDeadline(LocalDate v)                   { deadline = v; }
    public void setIcon(String v)                          { icon = v; }
    public void setProgressPercent(double v)               { progressPercent = v; }
    public void setMonthsToCompletion(Integer v)           { monthsToCompletion = v; }
    public void setEstimatedCompletionDate(LocalDate v)    { estimatedCompletionDate = v; }
    public void setCreatedAt(LocalDateTime v)              { createdAt = v; }
}
