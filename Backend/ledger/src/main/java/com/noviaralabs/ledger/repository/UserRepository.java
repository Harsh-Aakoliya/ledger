package com.noviaralabs.ledger.repository;

import com.noviaralabs.ledger.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByMobileNumber(String mobileNumber);
}
