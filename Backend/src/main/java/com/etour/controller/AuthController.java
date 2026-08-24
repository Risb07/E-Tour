package com.etour.controller;

import org.springframework.web.bind.annotation.*;

import com.etour.dto.LoginRequest;
import com.etour.dto.LoginResponse;
import com.etour.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}