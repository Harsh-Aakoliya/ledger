package com.noviaralabs.ledger.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "banks")
public class Bank {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_id")
    private Long id;

    @Column(name = "bank_name", nullable = false)
    private String name;

    public Long getId() { return id; }
    public String getName() { return name; }
}
