package com.votify.frontend.client;

// Resultado de una comprobación de acceso del frontend.
public record AccessDecision(boolean allowed, String message) {
    // Crea una decisión positiva sin mensaje de error.
    public static AccessDecision allow() {
        return new AccessDecision(true, "");
    }

    // Crea una decisión negativa con el motivo recibido.
    public static AccessDecision deny(String message) {
        return new AccessDecision(false, message);
    }
}
