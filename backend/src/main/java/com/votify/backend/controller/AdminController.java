package com.votify.backend.controller;

import com.votify.backend.dto.AdminEventRequest;
import com.votify.backend.dto.AdminEventResponse;
import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.service.EventAdminService;
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
    private final EventAdminService eventAdminService;

    // Inyecta el servicio de ajustes usado por el panel de administración.
    public AdminController(EventSettingsService eventSettingsService, EventAdminService eventAdminService) {
        this.eventSettingsService = eventSettingsService;
        this.eventAdminService = eventAdminService;
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

    @GetMapping("/events")
    // Devuelve eventos para el dashboard administrativo.
    public ResponseEntity<java.util.List<AdminEventResponse>> getEvents() {
        return ResponseEntity.ok(eventAdminService.findAll());
    }

    @PostMapping("/events")
    // Crea un nuevo evento y lo deja activo.
    public ResponseEntity<AdminEventResponse> createEvent(@RequestBody AdminEventRequest request) {
        return ResponseEntity.status(201).body(eventAdminService.create(request));
    }

    @PutMapping("/events/{id}")
    // Actualiza los ajustes de un evento existente.
    public ResponseEntity<AdminEventResponse> updateEvent(@PathVariable Long id, @RequestBody AdminEventRequest request) {
        return ResponseEntity.ok(eventAdminService.update(id, request));
    }

    @DeleteMapping("/events/{id}")
    // Elimina un evento existente y todos sus datos asociados.
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventAdminService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{id}/archive")
    // Archiva un evento existente dejándolo inactivo.
    public ResponseEntity<Void> archiveEvent(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE events SET active = false WHERE id = ?", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    // Borra votos y participantes manteniendo usuarios y configuración.
    public ResponseEntity<Void> resetEvent() {
        Long eventId = eventSettingsService.getActiveEvent().getId();
        jdbcTemplate.update("DELETE FROM votes WHERE event_id = ?", eventId);
        jdbcTemplate.update("""
                DELETE FROM participant_members
                WHERE participant_id IN (SELECT id FROM participants WHERE event_id = ?)
                """, eventId);
        jdbcTemplate.update("DELETE FROM participants WHERE event_id = ?", eventId);
        
        return ResponseEntity.ok().build();
    }
}
