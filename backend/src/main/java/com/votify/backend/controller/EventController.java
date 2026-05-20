package com.votify.backend.controller;

import com.votify.backend.dto.EventResponse;
import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.service.EventSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event")
// Expone información pública de configuración del evento.
public class EventController {
    private final EventSettingsService eventSettingsService;

    // Inyecta el servicio de ajustes del evento.
    public EventController(EventSettingsService eventSettingsService) {
        this.eventSettingsService = eventSettingsService;
    }

    @GetMapping("/settings")
    // Devuelve los ajustes actuales del evento.
    public ResponseEntity<EventSettingsDto> getSettings() {
        return ResponseEntity.ok(eventSettingsService.getSettings());
    }

    @GetMapping
    // Devuelve los eventos activos seleccionables por usuarios.
    public ResponseEntity<List<EventResponse>> getEvents() {
        return ResponseEntity.ok(eventSettingsService.getPublicEvents().stream()
                .map(this::toResponse)
                .toList());
    }

    // Convierte un evento persistente en DTO publico.
    private EventResponse toResponse(EventEntity event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getEventDate(),
                event.getDescription(),
                event.isRegistrationsOpen(),
                event.isVotingOpen(),
                event.isResultsVisible(),
                event.getMaxTeamsToVote(),
                event.isJuryEnabled(),
                event.getPhase(),
                event.isActive()
        );
    }
}
