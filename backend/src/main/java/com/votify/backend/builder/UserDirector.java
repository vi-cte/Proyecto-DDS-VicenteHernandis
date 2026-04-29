package com.votify.backend.builder;

import com.votify.backend.entity.User;
import org.springframework.lang.NonNull;

// Director que fija el orden de creación de un usuario.
public class UserDirector {
    private final UserBuilder builder;

    // Recibe el builder concreto que realizará la construcción.
    public UserDirector(UserBuilder builder) {
        this.builder = builder;
    }

    // Construye un usuario registrado con correo y contraseña.
    @NonNull
    public User buildRegisteredUser(String email, String password) {
        return builder
                .email(email)
                .password(password)
                .build();
    }
}
