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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST para operaciones de votacion en el API /api.
@RestController
@RequestMapping("/api")
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    // POST /api/votes: registra votos y devuelve 201 Created.
    @PostMapping("/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse createVotes(@Valid @RequestBody VoteRequest request) {
        return voteService.createVotes(request);
    }

    // GET /api/results: devuelve resultados agregados.
    @GetMapping("/results")
    public ResultsResponse getResults() {
        return voteService.getResults();
    }

    @GetMapping("/votes/settings")
    public VoteSettingsResponse getVoteSettings() {
        return voteService.getVoteSettings();
    }
}
