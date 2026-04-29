package com.votify.backend.controller;

import com.votify.backend.dto.ResultsResponse;
import com.votify.backend.dto.VoteRequest;
import com.votify.backend.dto.VoteResponse;
import com.votify.backend.dto.VoteSettingsResponse;
import com.votify.backend.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST para operaciones de votacion en el API /api.
@RestController
@RequestMapping("/api")
public class VoteController {
    private final VoteService voteService;

    // Inyecta el servicio que gestiona votos y resultados.
    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    // POST /api/votes: registra votos y devuelve 201 Created.
    @PostMapping("/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse createVotes(
            @Valid @RequestBody VoteRequest request,
            @RequestHeader("X-User-ID") Long userId
    ) {
        return voteService.createVotes(request, userId);
    }

    // GET /api/results: devuelve resultados agregados.
    @GetMapping("/results")
    public ResultsResponse getResults() {
        return voteService.getResults();
    }

    @GetMapping("/votes/settings")
    // GET /api/votes/settings: devuelve el límite de equipos votables.
    public VoteSettingsResponse getVoteSettings() {
        return voteService.getVoteSettings();
    }

    // GET /api/votes/has-voted: comprueba si un usuario ya ha votado.
    @GetMapping("/votes/has-voted")
    public boolean hasVoted(@RequestHeader("X-User-ID") Long userId) {
        return voteService.hasUserVoted(userId);
    }
}
