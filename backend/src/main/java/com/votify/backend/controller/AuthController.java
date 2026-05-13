package com.votify.backend.controller;

import com.votify.backend.dto.AuthRequest;
import com.votify.backend.dto.AuthResponse;
import com.votify.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// Expone endpoints de registro e inicio de sesión.
public class AuthController {
    private final AuthService authService;

    // Inyecta el servicio de autenticación.
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    // Registra un usuario nuevo y responde con sus datos de sesión.
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        try { return ResponseEntity.ok(authService.register(request)); } 
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, e.getMessage())); }
    }

    @PostMapping("/login")
    // Valida credenciales y responde con los datos de sesión.
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try { return ResponseEntity.ok(authService.login(request)); } 
        catch (RuntimeException e) { return ResponseEntity.status(401).body(new AuthResponse(null, null, null, e.getMessage())); }
    }
}
