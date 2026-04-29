package com.votify.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
// Entidad JPA que representa un usuario registrado.
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;

    // Devuelve el identificador generado del usuario.
    public Long getId() {
        return id;
    }

    // Devuelve el correo del usuario.
    public String getEmail() {
        return email;
    }

    // Actualiza el correo del usuario.
    public void setEmail(String email) {
        this.email = email;
    }

    // Devuelve la contraseña almacenada del usuario.
    public String getPassword() {
        return password;
    }

    // Actualiza la contraseña almacenada del usuario.
    public void setPassword(String password) {
        this.password = password;
    }
}
