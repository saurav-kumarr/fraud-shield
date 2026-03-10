package com.fraudshield.transaction.repository;

import com.fraudshield.transaction.model.Transaction;
import com.fraudshield.transaction.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByUserId(String userId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByUserIdAndCreatedAtBetween(
            String userId,
            LocalDateTime start,
            LocalDateTime end
    );
    long countByUserIdAndCreatedAtAfter(
            String userId,
            LocalDateTime after
    );
}
