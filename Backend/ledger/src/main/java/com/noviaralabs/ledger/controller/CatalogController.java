package com.noviaralabs.ledger.controller;

import com.noviaralabs.ledger.dto.Responses.*;
import com.noviaralabs.ledger.service.LedgerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final LedgerService service;

    public CatalogController(LedgerService service) { this.service = service; }

    @GetMapping("/banks")
    public List<BankDto> banks() { return service.allBanks(); }

    @GetMapping("/categories")
    public List<CategoryDto> categories() { return service.allCategories(); }
}
