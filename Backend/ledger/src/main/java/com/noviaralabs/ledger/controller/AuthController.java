package com.noviaralabs.ledger.controller;

import com.noviaralabs.ledger.dto.Requests.*;
import com.noviaralabs.ledger.dto.Responses.UserDto;
import com.noviaralabs.ledger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService service;

    public AuthController(UserService service) { this.service = service; }

    /** 200 with the user, or 404 if the number is not registered. */
    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginRequest r) { return service.login(r.mobileNumber()); }

    @PostMapping("/register")
    public UserDto register(@Valid @RequestBody RegisterRequest r) { return service.register(r.mobileNumber(), r.name()); }
}
