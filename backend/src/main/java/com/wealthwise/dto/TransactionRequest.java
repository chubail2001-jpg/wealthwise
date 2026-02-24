package com.wealthwise.dto;

import com.wealthwise.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private String description;
    private String category;
    private LocalDate date;

    public TransactionRequest() {}
    public TransactionRequest(Transaction.TransactionType type, BigDecimal amount,
                              String description, String category, LocalDate date) {
        this.type = type; this.amount = amount; this.description = description;
        this.category = category; this.date = date;
    }

    public Transaction.TransactionType getType()        { return type; }
    public BigDecimal                  getAmount()      { return amount; }
    public String                      getDescription() { return description; }
    public String                      getCategory()    { return category; }
    public LocalDate                   getDate()        { return date; }

    public void setType(Transaction.TransactionType t) { type        = t; }
    public void setAmount(BigDecimal a)                { amount      = a; }
    public void setDescription(String d)               { description = d; }
    public void setCategory(String c)                  { category    = c; }
    public void setDate(LocalDate d)                   { date        = d; }
}
