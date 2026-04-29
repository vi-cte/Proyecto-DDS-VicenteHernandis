package com.votify.backend.exception;

import org.springframework.http.HttpStatus;

// Excepción de negocio que conserva el estado HTTP que debe devolverse.
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    // Crea una excepción con estado HTTP y mensaje de error.
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    // Devuelve el estado HTTP asociado al error.
    public HttpStatus getStatus() {
        return status;
    }
}
