package com.votify.backend.controller;

import com.votify.backend.dto.ResultsResponse;
import com.votify.backend.dto.VoteRequest;
import com.votify.backend.dto.VoteResponse;
import com.votify.backend.dto.VoteSettingsResponse;
import com.votify.backend.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controlador REST para operaciones de votacion en el API /api.
@RestController
@RequestMapping("/api")
public class VoteController {
    private final VoteService voteService;
    private final String adminPassword;

    // Inyecta el servicio que gestiona votos y resultados.
    public VoteController(
            VoteService voteService,
            @Value("${votify.admin.password:admin123}") String adminPassword
    ) {
        this.voteService = voteService;
        this.adminPassword = adminPassword;
    }

    // POST /api/votes: registra votos y devuelve 201 Created.
    @PostMapping("/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse createVotes(
            @Valid @RequestBody VoteRequest request,
            @RequestHeader("X-User-ID") Long userId,
            @RequestParam(required = false) Long eventId
    ) {
        return voteService.createVotes(request, userId, eventId);
    }

    // GET /api/results: devuelve resultados agregados.
    @GetMapping("/results")
    public ResultsResponse getResults(
            @RequestParam(required = false) Long eventId,
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestHeader(value = "X-Admin-Password", required = false) String providedAdminPassword
    ) {
        return voteService.getResults(eventId, userId, isValidAdminPassword(providedAdminPassword));
    }

    @GetMapping("/votes/settings")
    // GET /api/votes/settings: devuelve el límite de equipos votables.
    public VoteSettingsResponse getVoteSettings(@RequestParam(required = false) Long eventId) {
        return voteService.getVoteSettings(eventId);
    }

    // GET /api/votes/has-voted: comprueba si un usuario ya ha votado.
    @GetMapping("/votes/has-voted")
    public boolean hasVoted(
            @RequestHeader("X-User-ID") Long userId,
            @RequestParam(required = false) Long eventId
    ) {
        return voteService.hasUserVoted(userId, eventId);
    }

    // GET /api/votes/evaluated-teams: equipos ya evaluados por el jurado multicriterio.
    @GetMapping("/votes/evaluated-teams")
    public List<String> getEvaluatedJuryTeams(
            @RequestHeader("X-User-ID") Long userId,
            @RequestParam(required = false) Long eventId
    ) {
        return voteService.getEvaluatedJuryTeamNames(userId, eventId);
    }

    private boolean isValidAdminPassword(String providedAdminPassword) {
        return providedAdminPassword != null
                && !providedAdminPassword.isBlank()
                && adminPassword.equals(providedAdminPassword);
    }
}
