package com.votify.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// Arranca la aplicación Spring Boot del backend de Votify.
public class VotifyBackendApplication {
    // Punto de entrada que delega el inicio en Spring Boot.
    public static void main(String[] args) {
        SpringApplication.run(VotifyBackendApplication.class, args);
    }
}
