package com.votify.frontend.client;

// Mantiene los datos de sesión local usados por el cliente API.
public class SessionManager {
    private String sessionToken;
    private String sessionEmail;
    private String sessionRole;
    private String adminPassword;

    // Guarda la sesión de usuario devuelta por el backend.
    public void startUserSession(String token, String email, String role) {
        this.sessionToken = token;
        this.sessionEmail = email;
        this.sessionRole = role == null || role.isBlank() ? "PUBLIC" : role;
    }

    // Guarda la contraseña admin validada para llamadas administrativas.
    public void startAdminSession(String password) {
        this.adminPassword = password;
    }

    // Limpia los datos de sesión de usuario.
    public void clearUserSession() {
        sessionToken = null;
        sessionEmail = null;
        sessionRole = null;
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

    // Indica si hay una sesión administrativa guardada.
    public boolean hasAdminSession() {
        return adminPassword != null && !adminPassword.isBlank();
    }

    // Devuelve el correo del usuario actualmente autenticado.
    public String getCurrentUserEmail() {
        return sessionEmail;
    }

    // Devuelve el rol del usuario actualmente autenticado.
    public String getCurrentUserRole() {
        return sessionRole == null || sessionRole.isBlank() ? "PUBLIC" : sessionRole;
    }

    // Indica si el usuario actual es jurado.
    public boolean isCurrentUserJury() {
        return "JURY".equalsIgnoreCase(getCurrentUserRole());
    }
}
