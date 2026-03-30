package com.votify.backend.dto;

import java.util.List;

// DTO de salida con resultados agregados de la votacion.
public record ResultsResponse(
        long totalVotes,
        List<ResultItemResponse> results
) {
}
