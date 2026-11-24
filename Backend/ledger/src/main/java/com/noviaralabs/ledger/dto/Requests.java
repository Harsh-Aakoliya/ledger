package com.noviaralabs.ledger.dto;

import com.noviaralabs.ledger.entity.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class Requests {
    private Requests() { }

    public record LoginRequest(@NotBlank String mobileNumber) { }

    public record RegisterRequest(@NotBlank String mobileNumber, @NotBlank String name) { }

    public record AddBankRequest(@NotNull Long bankId, @NotNull BigDecimal openingBalance) { }

    public record UpdateBankRequest(@NotNull BigDecimal openingBalance) { }

    public record TransactionRequest(
            @NotNull Long bankUserId,
            @NotNull TransactionType type,
            @NotNull @Positive BigDecimal amount,
            @NotNull Long categoryId,
            @Size(max = 500) String details,
            @NotNull LocalDate transactionDate) { }
}
