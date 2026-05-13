package com.votify.backend.builder;

import com.votify.backend.entity.User;
import com.votify.backend.entity.UserRole;
import org.springframework.lang.NonNull;

// Builder concreto que guarda los datos necesarios para crear un usuario.
public class DefaultUserBuilder implements UserBuilder {
    private String email;
    private String password;
    private UserRole role = UserRole.PUBLIC;

    @Override
    // Asigna el correo al usuario en construcción.
    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    @Override
    // Asigna la contraseña ya procesada al usuario en construcción.
    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }

    @Override
    // Asigna el rol al usuario en construcción.
    public UserBuilder role(UserRole role) {
        this.role = role == null ? UserRole.PUBLIC : role;
        return this;
    }

    @Override
    // Crea la entidad usuario final con los datos acumulados.
    @NonNull
    public User build() {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
