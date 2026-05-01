package com.votify.frontend.client;

public final class FormValidators {

    public static final String MSG_INVALID_EMAIL = "Formato de correo no válido.";
    public static final String MSG_SHORT_PASSWORD = "La contraseña debe tener al menos 8 caracteres.";
    public static final String MSG_REQUIRED_FIELDS = "Por favor, rellena todos los campos.";
    public static final String MSG_TEAM_REQUIRED = "El nombre del equipo es obligatorio.";
    public static final String MSG_TEAM_EXISTS = "El nombre del equipo ya está registrado.";
    public static final String MSG_EMAIL_REQUIRED = "El correo es obligatorio.";

    private FormValidators() {
        // Clase utilitaria, no instanciable
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}