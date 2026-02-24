package com.wealthwise.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotBlank
    private String description;

    private String category;

    @NotNull
    private LocalDate date;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        INCOME, EXPENSE, SAVING, INVESTMENT
    }

    public Transaction() {}

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long            getId()          { return id; }
    public User            getUser()        { return user; }
    public TransactionType getType()        { return type; }
    public BigDecimal      getAmount()      { return amount; }
    public String          getDescription() { return description; }
    public String          getCategory()    { return category; }
    public LocalDate       getDate()        { return date; }
    public LocalDateTime   getCreatedAt()   { return createdAt; }

    public void setId(Long id)                     { this.id = id; }
    public void setUser(User user)                 { this.user = user; }
    public void setType(TransactionType type)      { this.type = type; }
    public void setAmount(BigDecimal amount)       { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category)       { this.category = category; }
    public void setDate(LocalDate date)            { this.date = date; }
}
