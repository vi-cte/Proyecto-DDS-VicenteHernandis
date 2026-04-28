package com.votify.backend.controller;

import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.service.EventSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final EventSettingsService eventSettingsService;

    public AdminController(EventSettingsService eventSettingsService) {
        this.eventSettingsService = eventSettingsService;
    }

    // Endpoint para que el cliente compruebe sus credenciales admin
    // El proxy (SecurityInterceptor) validará la seguridad antes de llegar aquí.
    @PostMapping("/auth")
    public ResponseEntity<Void> authenticateAdmin() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<EventSettingsDto> getSettings() {
        return ResponseEntity.ok(eventSettingsService.getSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<Void> updateSettings(@RequestBody EventSettingsDto settings) {
        eventSettingsService.updateSettings(settings);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/phase/registration")
    public ResponseEntity<Void> openRegistrations(@RequestBody EventSettingsDto settings) {
        eventSettingsService.openRegistrations(settings.resultsVisible(), settings.maxTeamsToVote());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/phase/voting")
    public ResponseEntity<Void> openVoting(@RequestBody EventSettingsDto settings) {
        eventSettingsService.openVoting(settings.resultsVisible(), settings.maxTeamsToVote());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/phase/closed")
    public ResponseEntity<Void> closeEvent(@RequestBody EventSettingsDto settings) {
        eventSettingsService.closeEvent(settings.resultsVisible(), settings.maxTeamsToVote());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetEvent() {
        // Se eliminan todos los datos relacionados con la votación, pero 
        // se mantienen las configuraciones del evento. y los usuarios de la aplicacion.
        jdbcTemplate.execute("DELETE FROM votes");
        jdbcTemplate.execute("DELETE FROM participant_members");
        jdbcTemplate.execute("DELETE FROM participants");
        
        return ResponseEntity.ok().build();
    }
}
