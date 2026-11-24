package com.noviaralabs.ledger.controller;

import com.noviaralabs.ledger.dto.Requests.*;
import com.noviaralabs.ledger.dto.Responses.DashboardDto;
import com.noviaralabs.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}")
public class LedgerController {
    private final LedgerService service;

    public LedgerController(LedgerService service) { this.service = service; }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(@PathVariable Long userId) { return service.dashboard(userId); }

    // ----- banks -----
    @PostMapping("/banks")
    public DashboardDto addBank(@PathVariable Long userId, @Valid @RequestBody AddBankRequest r) {
        return service.addBank(userId, r);
    }

    @PutMapping("/banks/{bankUserId}")
    public DashboardDto updateBank(@PathVariable Long userId, @PathVariable Long bankUserId,
                                   @Valid @RequestBody UpdateBankRequest r) {
        return service.updateBank(userId, bankUserId, r);
    }

    @DeleteMapping("/banks/{bankUserId}")
    public DashboardDto removeBank(@PathVariable Long userId, @PathVariable Long bankUserId) {
        return service.removeBank(userId, bankUserId);
    }

    // ----- transactions -----
    @PostMapping("/transactions")
    public DashboardDto addTransaction(@PathVariable Long userId, @Valid @RequestBody TransactionRequest r) {
        return service.addTransaction(userId, r);
    }

    @PutMapping("/transactions/{id}")
    public DashboardDto updateTransaction(@PathVariable Long userId, @PathVariable Long id,
                                          @Valid @RequestBody TransactionRequest r) {
        return service.updateTransaction(userId, id, r);
    }

    @DeleteMapping("/transactions/{id}")
    public DashboardDto deleteTransaction(@PathVariable Long userId, @PathVariable Long id) {
        return service.deleteTransaction(userId, id);
    }
}
