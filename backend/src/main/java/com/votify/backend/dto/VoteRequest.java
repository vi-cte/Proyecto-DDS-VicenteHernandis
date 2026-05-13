package com.votify.backend.dto;

import java.util.List;

// DTO de entrada para registrar votos.
public record VoteRequest(
        List<String> selections,
        String juryWinnerSelection,
        String juryTechnicalSelection
) {
    // Constructor de compatibilidad para votos públicos existentes.
    public VoteRequest(List<String> selections) {
        this(selections, null, null);
    }
}
