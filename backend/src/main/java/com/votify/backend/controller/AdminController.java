package com.votify.backend.controller;

import com.votify.backend.dto.AdminEventRequest;
import com.votify.backend.dto.AdminEventResponse;
import com.votify.backend.dto.AuthRequest;
import com.votify.backend.dto.AuthResponse;
import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.observer.AdminDashboardSseObserver;
import com.votify.backend.observer.VoteEventPublisher;
import com.votify.backend.service.AuthService;
import com.votify.backend.service.EventAdminService;
import com.votify.backend.service.EventSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin")
// Expone endpoints administrativos para autenticación, ajustes y reinicio del evento.
public class AdminController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final EventSettingsService eventSettingsService;
    private final EventAdminService eventAdminService;
    private final AuthService authService;
    private final AdminDashboardSseObserver adminDashboardSseObserver;
    private final VoteEventPublisher voteEventPublisher;

    // Inyecta el servicio de ajustes usado por el panel de administración.
    public AdminController(EventSettingsService eventSettingsService, EventAdminService eventAdminService, AuthService authService, AdminDashboardSseObserver adminDashboardSseObserver, VoteEventPublisher voteEventPublisher) {
        this.eventSettingsService = eventSettingsService;
        this.eventAdminService = eventAdminService;
        this.authService = authService;
        this.adminDashboardSseObserver = adminDashboardSseObserver;
        this.voteEventPublisher = voteEventPublisher;
    }

    // Endpoint para que el cliente compruebe sus credenciales admin
    // El proxy (SecurityInterceptor) validará la seguridad antes de llegar aquí.
    @PostMapping("/auth")
    public ResponseEntity<Void> authenticateAdmin() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/jury")
    // Crea una cuenta de jurado desde el panel de administración.
    public ResponseEntity<AuthResponse> createJury(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.status(201).body(authService.register(new AuthRequest(
                    request.email(),
                    request.password(),
                    "JURY"
            )));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, null, e.getMessage()));
        }
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

    @GetMapping("/events/stream")
    // Suscribe el dashboard admin a cambios de votación en tiempo real.
    public SseEmitter streamEventUpdates() {
        return adminDashboardSseObserver.subscribe();
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
    public ResponseEntity<Void> deleteEvent(@PathVariable @NonNull Long id) {
        eventAdminService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{id}/archive")
    // Archiva un evento existente dejándolo inactivo.
    public ResponseEntity<Void> archiveEvent(@PathVariable @NonNull Long id) {
        eventAdminService.archive(id);
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
        voteEventPublisher.notifyVotesChanged(eventId);
        
        return ResponseEntity.ok().build();
    }
}
