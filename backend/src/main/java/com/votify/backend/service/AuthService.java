package com.votify.backend.service;

import com.votify.backend.builder.DefaultUserBuilder;
import com.votify.backend.builder.UserDirector;
import com.votify.backend.dto.AuthRequest;
import com.votify.backend.dto.AuthResponse;
import com.votify.backend.entity.User;
import com.votify.backend.entity.UserRole;
import com.votify.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
// Gestiona registro, login y cifrado simple de contraseñas.
public class AuthService {
    private final UserRepository userRepository;

    // Inyecta el repositorio de usuarios.
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Registra un usuario nuevo tras validar contraseña y correo único.
    public AuthResponse register(AuthRequest request) {
        if (request.password() == null || request.password().length() < 8) {
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }
        UserDirector director = new UserDirector(new DefaultUserBuilder());
        User user = director.buildRegisteredUser(request.email(), hashPassword(request.password()), parseRole(request.role()));
        userRepository.save(user);
        return new AuthResponse(user.getId().toString(), user.getEmail(), user.getRole().name(), "Registro exitoso");
    }

    // Comprueba credenciales y devuelve una respuesta de sesión si son válidas.
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> u.getPassword().equals(hashPassword(request.password())))
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        return new AuthResponse(user.getId().toString(), user.getEmail(), user.getRole().name(), "Login exitoso");
    }

    // Convierte el texto recibido en un rol válido; por defecto crea usuarios públicos.
    private UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return UserRole.PUBLIC;
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de usuario no válido");
        }
    }

    // Calcula el hash SHA-256 en Base64 de la contraseña recibida.
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException("Error interno", e); }
    }
}
