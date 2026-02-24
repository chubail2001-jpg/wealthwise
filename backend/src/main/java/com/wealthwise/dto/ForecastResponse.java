package com.wealthwise.dto;

import java.util.List;

public class ForecastResponse {

    private List<MonthData> historical;
    private List<MonthData> projected;
    private double averageMonthlyNet;
    private double currentBalance;

    public ForecastResponse() {}

    public ForecastResponse(List<MonthData> historical, List<MonthData> projected,
                            double averageMonthlyNet, double currentBalance) {
        this.historical = historical;
        this.projected = projected;
        this.averageMonthlyNet = averageMonthlyNet;
        this.currentBalance = currentBalance;
    }

    public List<MonthData> getHistorical()      { return historical; }
    public List<MonthData> getProjected()        { return projected; }
    public double          getAverageMonthlyNet(){ return averageMonthlyNet; }
    public double          getCurrentBalance()   { return currentBalance; }

    public void setHistorical(List<MonthData> v)    { historical = v; }
    public void setProjected(List<MonthData> v)      { projected = v; }
    public void setAverageMonthlyNet(double v)        { averageMonthlyNet = v; }
    public void setCurrentBalance(double v)           { currentBalance = v; }

    public static class MonthData {
        private String label;
        private double income;
        private double expense;
        private double saving;
        private double investment;
        private double balance;
        private boolean projected;

        public MonthData() {}

        public MonthData(String label, double income, double expense,
                         double saving, double investment, double balance, boolean projected) {
            this.label = label;
            this.income = income;
            this.expense = expense;
            this.saving = saving;
            this.investment = investment;
            this.balance = balance;
            this.projected = projected;
        }

        public String  getLabel()      { return label; }
        public double  getIncome()     { return income; }
        public double  getExpense()    { return expense; }
        public double  getSaving()     { return saving; }
        public double  getInvestment() { return investment; }
        public double  getBalance()    { return balance; }
        public boolean isProjected()   { return projected; }

        public void setLabel(String v)      { label = v; }
        public void setIncome(double v)     { income = v; }
        public void setExpense(double v)    { expense = v; }
        public void setSaving(double v)     { saving = v; }
        public void setInvestment(double v) { investment = v; }
        public void setBalance(double v)    { balance = v; }
        public void setProjected(boolean v) { projected = v; }
    }
}
