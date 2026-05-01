package com.votify.frontend.client;

// Mantiene los datos de sesión local usados por el cliente API.
public class SessionManager {
    private String sessionToken;
    private String sessionEmail;
    private String adminPassword;

    // Guarda la sesión de usuario devuelta por el backend.
    public void startUserSession(String token, String email) {
        this.sessionToken = token;
        this.sessionEmail = email;
    }

    // Guarda la contraseña admin validada para llamadas administrativas.
    public void startAdminSession(String password) {
        this.adminPassword = password;
    }

    // Limpia los datos de sesión de usuario.
    public void clearUserSession() {
        sessionToken = null;
        sessionEmail = null;
    }

    // Devuelve el valor seguro para la cabecera de usuario.
    public String userIdHeaderValue() {
        return sessionToken == null ? "" : sessionToken;
    }

    // Devuelve el valor seguro para la cabecera de administrador.
    public String adminPasswordHeaderValue() {
        return adminPassword == null ? "" : adminPassword;
    }

    // Indica si hay una sesión de usuario guardada localmente.
    public boolean isUserLoggedIn() {
        return sessionToken != null && !sessionToken.isBlank()
                && sessionEmail != null && !sessionEmail.isBlank();
    }

    // Indica si existe token de usuario.
    public boolean hasUserToken() {
        return sessionToken != null;
    }

    // Devuelve el correo del usuario actualmente autenticado.
    public String getCurrentUserEmail() {
        return sessionEmail;
    }
}
