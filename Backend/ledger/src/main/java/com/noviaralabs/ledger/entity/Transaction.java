package com.noviaralabs.ledger.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_user_id")
    private BankUser bankUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type;

    @Column(name = "transaction_amount", nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_category_id")
    private TransactionCategory category;

    @Column(name = "transaction_details")
    private String details;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public BankUser getBankUser() { return bankUser; }
    public void setBankUser(BankUser bankUser) { this.bankUser = bankUser; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionCategory getCategory() { return category; }
    public void setCategory(TransactionCategory category) { this.category = category; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
