package com.votify.backend.controller;

import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.service.EventSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
// Expone endpoints administrativos para autenticación, ajustes y reinicio del evento.
public class AdminController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final EventSettingsService eventSettingsService;

    // Inyecta el servicio de ajustes usado por el panel de administración.
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
    // Devuelve la configuración actual del evento para el panel admin.
    public ResponseEntity<EventSettingsDto> getSettings() {
        return ResponseEntity.ok(eventSettingsService.getSettings());
    }

    @PutMapping("/settings")
    // Actualiza la configuración del evento desde el panel admin.
    public ResponseEntity<Void> updateSettings(@RequestBody EventSettingsDto settings) {
        eventSettingsService.updateSettings(settings);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    // Borra votos y participantes manteniendo usuarios y configuración.
    public ResponseEntity<Void> resetEvent() {
        // Se eliminan todos los datos relacionados con la votación, pero 
        // se mantienen las configuraciones del evento. y los usuarios de la aplicacion.
        jdbcTemplate.execute("DELETE FROM votes");
        jdbcTemplate.execute("DELETE FROM participant_members");
        jdbcTemplate.execute("DELETE FROM participants");
        
        return ResponseEntity.ok().build();
    }
}
