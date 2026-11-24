package com.noviaralabs.ledger.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "mobile_number", nullable = false, unique = true)
    private String mobileNumber;

    @Column(nullable = false)
    private String name;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist void onCreate() { registeredAt = updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getRegisteredAt() { return registeredAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
