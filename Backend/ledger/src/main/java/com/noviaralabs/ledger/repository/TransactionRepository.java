package com.noviaralabs.ledger.repository;

import com.noviaralabs.ledger.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t from Transaction t join fetch t.bankUser bu join fetch bu.bank join fetch t.category "
         + "where bu.userId = :userId order by t.transactionDate desc, t.id desc")
    List<Transaction> findAllByUser(@Param("userId") Long userId);

    @Query("select t from Transaction t where t.id = :id and t.bankUser.userId = :userId")
    Optional<Transaction> findOwned(@Param("id") Long id, @Param("userId") Long userId);
}
