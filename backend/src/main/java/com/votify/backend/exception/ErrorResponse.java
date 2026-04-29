package com.votify.backend.exception;

import java.time.Instant;

// Cuerpo estándar usado para devolver errores desde la API.
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
