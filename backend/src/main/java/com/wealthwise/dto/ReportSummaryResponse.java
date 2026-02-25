package com.wealthwise.dto;

import java.util.List;

public class ReportSummaryResponse {

    private String month;
    private String monthLabel;
    private double totalIncome;
    private double totalExpenses;
    private double totalSavings;
    private double totalInvestments;
    private double netSavings;
    private double savingsRate;
    private int    transactionCount;
    private List<CategoryEntry> topCategories;

    public static class CategoryEntry {
        private String category;
        private double amount;
        private double pct;

        public CategoryEntry() {}
        public CategoryEntry(String category, double amount, double pct) {
            this.category = category;
            this.amount   = amount;
            this.pct      = pct;
        }

        public String getCategory() { return category; }
        public double getAmount()   { return amount; }
        public double getPct()      { return pct; }
        public void setCategory(String c) { category = c; }
        public void setAmount(double a)   { amount = a; }
        public void setPct(double p)      { pct = p; }
    }

    public ReportSummaryResponse() {}

    public String  getMonth()            { return month; }
    public String  getMonthLabel()       { return monthLabel; }
    public double  getTotalIncome()      { return totalIncome; }
    public double  getTotalExpenses()    { return totalExpenses; }
    public double  getTotalSavings()     { return totalSavings; }
    public double  getTotalInvestments() { return totalInvestments; }
    public double  getNetSavings()       { return netSavings; }
    public double  getSavingsRate()      { return savingsRate; }
    public int     getTransactionCount() { return transactionCount; }
    public List<CategoryEntry> getTopCategories() { return topCategories; }

    public void setMonth(String v)            { month = v; }
    public void setMonthLabel(String v)       { monthLabel = v; }
    public void setTotalIncome(double v)      { totalIncome = v; }
    public void setTotalExpenses(double v)    { totalExpenses = v; }
    public void setTotalSavings(double v)     { totalSavings = v; }
    public void setTotalInvestments(double v) { totalInvestments = v; }
    public void setNetSavings(double v)       { netSavings = v; }
    public void setSavingsRate(double v)      { savingsRate = v; }
    public void setTransactionCount(int v)    { transactionCount = v; }
    public void setTopCategories(List<CategoryEntry> v) { topCategories = v; }
}
