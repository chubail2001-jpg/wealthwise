package com.wealthwise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalRequest {

    private String     name;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private BigDecimal monthlyContribution;
    private LocalDate  deadline;
    private String     icon;

    public GoalRequest() {}

    public String     getName()                { return name; }
    public BigDecimal getTargetAmount()        { return targetAmount; }
    public BigDecimal getSavedAmount()         { return savedAmount; }
    public BigDecimal getMonthlyContribution() { return monthlyContribution; }
    public LocalDate  getDeadline()            { return deadline; }
    public String     getIcon()                { return icon; }

    public void setName(String v)                    { name = v; }
    public void setTargetAmount(BigDecimal v)        { targetAmount = v; }
    public void setSavedAmount(BigDecimal v)         { savedAmount = v; }
    public void setMonthlyContribution(BigDecimal v) { monthlyContribution = v; }
    public void setDeadline(LocalDate v)             { deadline = v; }
    public void setIcon(String v)                    { icon = v; }
}
