package com.votify.backend.builder;

import com.votify.backend.entity.User;
import com.votify.backend.entity.UserRole;
import org.springframework.lang.NonNull;

// Define los pasos que debe implementar cualquier builder de usuarios.
public interface UserBuilder {
    // Recibe el correo del usuario para la construcción.
    UserBuilder email(String email);

    // Recibe la contraseña del usuario para la construcción.
    UserBuilder password(String password);

    // Recibe el rol del usuario para la construcción.
    UserBuilder role(UserRole role);

    // Devuelve la entidad usuario construida.
    @NonNull
    User build();
}
