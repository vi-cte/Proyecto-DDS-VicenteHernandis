package com.votify.backend.dto;

import java.util.List;

// DTO de salida al registrar votos.
public record VoteResponse(
        int recordedVotes,
        List<String> selections
) {
}
