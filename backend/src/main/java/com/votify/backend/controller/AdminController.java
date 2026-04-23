package com.votify.backend.controller;

import com.votify.backend.dto.EventSettingsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Estado simple en memoria. (En una app en producción, podría conectarse a tu BD)
    private boolean registrationsOpen = true;
    private boolean votingOpen = false;
    private int maxTeamsToVote = 1;

    @GetMapping("/settings")
    public ResponseEntity<EventSettingsDto> getSettings() {
        return ResponseEntity.ok(new EventSettingsDto(registrationsOpen, votingOpen, maxTeamsToVote));
    }

    @PutMapping("/settings")
    public ResponseEntity<Void> updateSettings(@RequestBody EventSettingsDto settings) {
        this.registrationsOpen = settings.registrationsOpen();
        this.votingOpen = settings.votingOpen();
        // Opciones excluyentes: si una está abierta, la otra debe estar cerrada por defecto.
        if (this.registrationsOpen && this.votingOpen) {
            this.votingOpen = false;
        }
        this.maxTeamsToVote = settings.maxTeamsToVote();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetEvent() {
        // IMPORTANTE: Asegúrate de que el nombre de estas tablas coincida con el generado en tu Base de Datos.
        jdbcTemplate.execute("DELETE FROM vote_selections"); 
        jdbcTemplate.execute("DELETE FROM vote");
        jdbcTemplate.execute("DELETE FROM participant_members");
        jdbcTemplate.execute("DELETE FROM participant");
        
        return ResponseEntity.ok().build();
    }
}