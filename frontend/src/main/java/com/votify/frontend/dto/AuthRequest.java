package com.votify.frontend.dto;

// DTO de frontend para enviar credenciales de autenticación.
public record AuthRequest(String email, String password) {
}
