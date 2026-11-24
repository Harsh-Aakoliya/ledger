package com.noviaralabs.ledger.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bank_users")
public class BankUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_user_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Column(name = "opening_balance", nullable = false)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void onCreate() { linkedAt = updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Bank getBank() { return bank; }
    public void setBank(Bank bank) { this.bank = bank; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getLinkedAt() { return linkedAt; }
    public void setLinkedAt(Instant linkedAt) { this.linkedAt = linkedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
