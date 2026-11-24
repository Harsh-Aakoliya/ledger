package com.noviaralabs.ledger.repository;

import com.noviaralabs.ledger.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> { }
