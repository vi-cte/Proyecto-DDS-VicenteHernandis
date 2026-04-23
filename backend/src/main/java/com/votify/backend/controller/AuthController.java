package com.votify.backend.controller;

import com.votify.backend.dto.AuthRequest;
import com.votify.backend.dto.AuthResponse;
import com.votify.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        try { return ResponseEntity.ok(authService.register(request)); } 
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(new AuthResponse(null, null, e.getMessage())); }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try { return ResponseEntity.ok(authService.login(request)); } 
        catch (RuntimeException e) { return ResponseEntity.status(401).body(new AuthResponse(null, null, e.getMessage())); }
    }
}