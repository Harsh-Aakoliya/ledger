package com.noviaralabs.ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class Responses {
    private Responses() { }

    public record UserDto(Long userId, String name, String mobileNumber) { }

    public record BankDto(Long bankId, String bankName) { }

    public record CategoryDto(Long categoryId, String category) { }

    public record UserBankDto(Long bankUserId, Long bankId, String bankName,
                              BigDecimal openingBalance, BigDecimal currentBalance) { }

    public record TransactionDto(Long transactionId, Long bankUserId, String bankName, String type,
                                 BigDecimal amount, Long categoryId, String category, String details,
                                 LocalDate transactionDate, Instant createdAt, Instant updatedAt) { }

    public record DashboardDto(UserDto user, List<UserBankDto> banks, List<TransactionDto> transactions) { }
}
