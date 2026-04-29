package com.votify.frontend.dto;

// DTO de frontend con la respuesta de autenticación.
public record AuthResponse(String token, String email, String message) {
}
