package com.noviaralabs.ledger.repository;

import com.noviaralabs.ledger.entity.BankUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BankUserRepository extends JpaRepository<BankUser, Long> {
    @EntityGraph(attributePaths = "bank")
    List<BankUser> findByUserIdAndActiveTrueOrderByLinkedAtAsc(Long userId);

    Optional<BankUser> findByUserIdAndBankId(Long userId, Long bankId);

    Optional<BankUser> findByIdAndUserId(Long id, Long userId);
}
