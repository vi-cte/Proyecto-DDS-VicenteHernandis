package com.votify.backend.controller;

import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.service.EventSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
