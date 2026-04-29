package com.votify.frontend.exception;

import java.time.Instant;

// DTO de frontend para leer errores estructurados enviados por el backend.
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // Constructor vacío requerido por Jackson.
    public ErrorResponse() {
    }

    // Devuelve la fecha del error.
    public Instant getTimestamp() {
        return timestamp;
    }

    // Actualiza la fecha del error.
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    // Devuelve el código HTTP del error.
    public int getStatus() {
        return status;
    }

    // Actualiza el código HTTP del error.
    public void setStatus(int status) {
        this.status = status;
    }

    // Devuelve el nombre HTTP del error.
    public String getError() {
        return error;
    }

    // Actualiza el nombre HTTP del error.
    public void setError(String error) {
        this.error = error;
    }

    // Devuelve el mensaje legible del error.
    public String getMessage() {
        return message;
    }

    // Actualiza el mensaje legible del error.
    public void setMessage(String message) {
        this.message = message;
    }

    // Devuelve la ruta que produjo el error.
    public String getPath() {
        return path;
    }

    // Actualiza la ruta que produjo el error.
    public void setPath(String path) {
        this.path = path;
    }
}
