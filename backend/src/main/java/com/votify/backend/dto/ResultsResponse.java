package com.votify.backend.dto;

import java.util.List;

// DTO de salida con resultados agregados de la votacion.
public record ResultsResponse(
        long totalVotes,
        List<ResultItemResponse> results,
        long totalPublicVotes,
        List<ResultItemResponse> publicResults,
        long totalJuryVotes,
        List<ResultItemResponse> juryResults
) {
    // Constructor de compatibilidad para consumidores que solo usan resultados generales.
    public ResultsResponse(long totalVotes, List<ResultItemResponse> results) {
        this(totalVotes, results, totalVotes, results, 0L, List.of());
    }
}
