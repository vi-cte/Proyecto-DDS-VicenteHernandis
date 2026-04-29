package com.votify.backend.repository;

import com.votify.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Repositorio JPA para acceder a usuarios.
public interface UserRepository extends JpaRepository<User, Long> {
    // Busca un usuario por su correo.
    Optional<User> findByEmail(String email);
}
