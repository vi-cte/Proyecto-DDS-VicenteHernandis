package com.votify.frontend.exception;

// Excepción usada por el cliente API para propagar errores HTTP o de conexión.
public class ApiClientException extends RuntimeException {
    // Crea una excepción con el mensaje que se mostrará al usuario.
    public ApiClientException(String message) {
        super(message);
    }
}
