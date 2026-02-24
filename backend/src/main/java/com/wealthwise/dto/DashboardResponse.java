package com.wealthwise.dto;

public class DashboardResponse {
    private double totalBalance;
    private double monthIncome;
    private double monthExpense;
    private double monthSaving;
    private double monthInvestment;

    public DashboardResponse() {}

    public DashboardResponse(double totalBalance, double monthIncome,
                             double monthExpense, double monthSaving, double monthInvestment) {
        this.totalBalance    = totalBalance;
        this.monthIncome     = monthIncome;
        this.monthExpense    = monthExpense;
        this.monthSaving     = monthSaving;
        this.monthInvestment = monthInvestment;
    }

    public double getTotalBalance()    { return totalBalance; }
    public double getMonthIncome()     { return monthIncome; }
    public double getMonthExpense()    { return monthExpense; }
    public double getMonthSaving()     { return monthSaving; }
    public double getMonthInvestment() { return monthInvestment; }

    public void setTotalBalance(double v)    { totalBalance    = v; }
    public void setMonthIncome(double v)     { monthIncome     = v; }
    public void setMonthExpense(double v)    { monthExpense    = v; }
    public void setMonthSaving(double v)     { monthSaving     = v; }
    public void setMonthInvestment(double v) { monthInvestment = v; }
}
