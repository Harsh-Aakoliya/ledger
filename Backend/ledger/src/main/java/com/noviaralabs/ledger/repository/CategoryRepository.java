package com.noviaralabs.ledger.repository;

import com.noviaralabs.ledger.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<TransactionCategory, Long> { }
