package com.noviaralabs.ledger.service;

import com.noviaralabs.ledger.dto.Requests.*;
import com.noviaralabs.ledger.dto.Responses.*;
import com.noviaralabs.ledger.entity.*;
import com.noviaralabs.ledger.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** All mutations return the fresh dashboard so the app can simply replace its state. */
@Service
public class LedgerService {
    private final UserRepository users;
    private final BankRepository banks;
    private final CategoryRepository categories;
    private final BankUserRepository bankUsers;
    private final TransactionRepository transactions;

    public LedgerService(UserRepository users, BankRepository banks, CategoryRepository categories,
                         BankUserRepository bankUsers, TransactionRepository transactions) {
        this.users = users;
        this.banks = banks;
        this.categories = categories;
        this.bankUsers = bankUsers;
        this.transactions = transactions;
    }

    // ---------- catalog ----------
    @Transactional(readOnly = true)
    public List<BankDto> allBanks() {
        return banks.findAll().stream().map(b -> new BankDto(b.getId(), b.getName())).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> allCategories() {
        return categories.findAll().stream().map(c -> new CategoryDto(c.getId(), c.getName())).toList();
    }

    // ---------- dashboard ----------
    @Transactional(readOnly = true)
    public DashboardDto dashboard(Long userId) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Transaction> txns = transactions.findAllByUser(userId);

        Map<Long, BigDecimal> net = new HashMap<>();
        for (Transaction t : txns) {
            BigDecimal signed = t.getType() == TransactionType.CREDIT ? t.getAmount() : t.getAmount().negate();
            net.merge(t.getBankUser().getId(), signed, BigDecimal::add);
        }

        List<UserBankDto> bankList = bankUsers.findByUserIdAndActiveTrueOrderByLinkedAtAsc(userId).stream()
                .map(bu -> new UserBankDto(bu.getId(), bu.getBank().getId(), bu.getBank().getName(),
                        bu.getOpeningBalance(),
                        bu.getOpeningBalance().add(net.getOrDefault(bu.getId(), BigDecimal.ZERO))))
                .toList();

        return new DashboardDto(new UserDto(user.getId(), user.getName(), user.getMobileNumber()),
                bankList, txns.stream().map(this::toDto).toList());
    }

    // ---------- banks ----------
    @Transactional
    public DashboardDto addBank(Long userId, AddBankRequest r) {
        ensureUser(userId);
        Bank bank = banks.findById(r.bankId()).orElseThrow(() -> bad("Bank not found"));
        BankUser bu = bankUsers.findByUserIdAndBankId(userId, r.bankId()).orElse(null);
        if (bu == null) {
            bu = new BankUser();
            bu.setUserId(userId);
            bu.setBank(bank);
        } else if (bu.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bank already linked");
        } else {
            bu.setLinkedAt(Instant.now()); // re-link a previously removed bank
        }
        bu.setActive(true);
        bu.setOpeningBalance(scale(r.openingBalance()));
        bankUsers.save(bu);
        return dashboard(userId);
    }

    @Transactional
    public DashboardDto updateBank(Long userId, Long bankUserId, UpdateBankRequest r) {
        BankUser bu = ownBank(userId, bankUserId);
        if (!bu.isActive()) throw bad("Bank is not active");
        bu.setOpeningBalance(scale(r.openingBalance()));
        bankUsers.save(bu);
        return dashboard(userId);
    }

    @Transactional
    public DashboardDto removeBank(Long userId, Long bankUserId) {
        BankUser bu = ownBank(userId, bankUserId);
        bu.setActive(false); // soft delete: transactions are kept
        bankUsers.save(bu);
        return dashboard(userId);
    }

    // ---------- transactions ----------
    @Transactional
    public DashboardDto addTransaction(Long userId, TransactionRequest r) {
        ensureUser(userId);
        Transaction t = new Transaction();
        apply(t, userId, r);
        transactions.save(t);
        return dashboard(userId);
    }

    @Transactional
    public DashboardDto updateTransaction(Long userId, Long id, TransactionRequest r) {
        Transaction t = ownTransaction(userId, id);
        apply(t, userId, r);
        transactions.save(t);
        return dashboard(userId);
    }

    @Transactional
    public DashboardDto deleteTransaction(Long userId, Long id) {
        transactions.delete(ownTransaction(userId, id)); // hard delete
        return dashboard(userId);
    }

    // ---------- helpers ----------
    private void apply(Transaction t, Long userId, TransactionRequest r) {
        BankUser bu = ownBank(userId, r.bankUserId());
        boolean sameBank = t.getBankUser() != null && t.getBankUser().getId().equals(bu.getId());
        if (!bu.isActive() && !sameBank) throw bad("Bank is not active");
        TransactionCategory c = categories.findById(r.categoryId()).orElseThrow(() -> bad("Category not found"));
        t.setBankUser(bu);
        t.setType(r.type());
        t.setAmount(scale(r.amount()));
        t.setCategory(c);
        String note = r.details() == null ? null : r.details().trim();
        t.setDetails(note == null || note.isEmpty() ? null : note);
        t.setTransactionDate(r.transactionDate());
    }

    private TransactionDto toDto(Transaction t) {
        BankUser bu = t.getBankUser();
        return new TransactionDto(t.getId(), bu.getId(), bu.getBank().getName(), t.getType().name(),
                t.getAmount(), t.getCategory().getId(), t.getCategory().getName(), t.getDetails(),
                t.getTransactionDate(), t.getCreatedAt(), t.getUpdatedAt());
    }

    private void ensureUser(Long userId) {
        if (!users.existsById(userId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    private BankUser ownBank(Long userId, Long bankUserId) {
        return bankUsers.findByIdAndUserId(bankUserId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank not found"));
    }

    private Transaction ownTransaction(Long userId, Long id) {
        return transactions.findOwned(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    private static BigDecimal scale(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    private static ResponseStatusException bad(String msg) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg); }
}
