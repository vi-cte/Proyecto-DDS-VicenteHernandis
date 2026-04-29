package com.votify.frontend.dto;

import java.util.List;

// DTO de frontend con el total de votos y el ranking recibido.
public class ResultsResponse {
    private long totalVotes;
    private List<ResultItemResponse> results;

    // Constructor vacío requerido por Jackson.
    public ResultsResponse() {
    }

    // Devuelve el total de votos registrados.
    public long getTotalVotes() {
        return totalVotes;
    }

    // Actualiza el total de votos registrados.
    public void setTotalVotes(long totalVotes) {
        this.totalVotes = totalVotes;
    }

    // Devuelve la lista de resultados por equipo.
    public List<ResultItemResponse> getResults() {
        return results;
    }

    // Actualiza la lista de resultados por equipo.
    public void setResults(List<ResultItemResponse> results) {
        this.results = results;
    }
}
