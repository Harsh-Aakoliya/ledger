package com.noviaralabs.ledger.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transaction_categories")
public class TransactionCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category", nullable = false)
    private String name;

    public Long getId() { return id; }
    public String getName() { return name; }
}
