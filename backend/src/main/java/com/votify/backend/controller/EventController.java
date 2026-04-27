package com.votify.backend.controller;

import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.service.EventSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final EventSettingsService eventSettingsService;

    public EventController(EventSettingsService eventSettingsService) {
        this.eventSettingsService = eventSettingsService;
    }

    @GetMapping("/settings")
    public ResponseEntity<EventSettingsDto> getSettings() {
        return ResponseEntity.ok(eventSettingsService.getSettings());
    }
}
