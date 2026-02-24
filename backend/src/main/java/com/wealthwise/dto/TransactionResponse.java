package com.wealthwise.dto;

import com.wealthwise.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private String description;
    private String category;
    private LocalDate date;
    private LocalDateTime createdAt;

    public TransactionResponse(Long id, Transaction.TransactionType type, BigDecimal amount,
                               String description, String category, LocalDate date,
                               LocalDateTime createdAt) {
        this.id = id; this.type = type; this.amount = amount;
        this.description = description; this.category = category;
        this.date = date; this.createdAt = createdAt;
    }

    public Long                        getId()          { return id; }
    public Transaction.TransactionType getType()        { return type; }
    public BigDecimal                  getAmount()      { return amount; }
    public String                      getDescription() { return description; }
    public String                      getCategory()    { return category; }
    public LocalDate                   getDate()        { return date; }
    public LocalDateTime               getCreatedAt()   { return createdAt; }
}
