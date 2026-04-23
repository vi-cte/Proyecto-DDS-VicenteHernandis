package com.votify.backend.controller;

import com.votify.backend.dto.ParticipantRequest;
import com.votify.backend.dto.ParticipantResponse;
import com.votify.backend.service.ParticipantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// Controlador REST para gestionar participantes mediante el API /api/participants.
@RestController
@RequestMapping("/api/participants")
public class ParticipantController {
    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    // GET /api/participants: devuelve la lista de participantes.
    @GetMapping
    public List<ParticipantResponse> getParticipants() {
        return participantService.findAll();
    }

    // GET /api/participants/exists?teamName=...: indica si existe el equipo.
    @GetMapping("/exists")
    public boolean existsByTeamName(@RequestParam String teamName) {
        return participantService.existsByTeamName(teamName == null ? "" : teamName.trim());
    }

    // POST /api/participants: crea un participante y devuelve 201 Created.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantResponse createParticipant(@Valid @RequestBody ParticipantRequest request) {
        return participantService.create(request);
    }

    // PUT /api/participants/{id}: actualiza un participante existente.
    @PutMapping("/{id}")
    public ParticipantResponse updateParticipant(@PathVariable Long id, @Valid @RequestBody ParticipantRequest request) {
        return participantService.update(id, request);
    }
}
