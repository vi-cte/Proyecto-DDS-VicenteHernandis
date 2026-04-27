package com.votify.backend.service;

import com.votify.backend.dto.AuthRequest;
import com.votify.backend.dto.AuthResponse;
import com.votify.backend.entity.User;
import com.votify.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(AuthRequest request) {
        if (request.password() == null || request.password().length() < 8) {
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(hashPassword(request.password()));
        userRepository.save(user);
        return new AuthResponse(user.getId().toString(), user.getEmail(), "Registro exitoso");
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> u.getPassword().equals(hashPassword(request.password())))
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        return new AuthResponse(user.getId().toString(), user.getEmail(), "Login exitoso");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException("Error interno", e); }
    }
}