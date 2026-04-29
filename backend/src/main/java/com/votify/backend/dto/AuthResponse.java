package com.votify.backend.dto;

// Respuesta de autenticación con token simple, correo y mensaje.
public record AuthResponse(
        String token,
        String email,
        String message
) {}
