package com.votify.frontend.dto;

import java.util.List;

// DTO de frontend con el total de votos y el ranking recibido.
public class ResultsResponse {
    private long totalVotes;
    private List<ResultItemResponse> results;
    private long totalPublicVotes;
    private List<ResultItemResponse> publicResults;
    private long totalJuryVotes;
    private List<ResultItemResponse> juryResults;
    private MyTeamResultsResponse myTeam;

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

    // Devuelve el total de votos del público.
    public long getTotalPublicVotes() {
        return totalPublicVotes;
    }

    // Actualiza el total de votos del público.
    public void setTotalPublicVotes(long totalPublicVotes) {
        this.totalPublicVotes = totalPublicVotes;
    }

    // Devuelve el ranking de ganadores del público.
    public List<ResultItemResponse> getPublicResults() {
        return publicResults;
    }

    // Actualiza el ranking de ganadores del público.
    public void setPublicResults(List<ResultItemResponse> publicResults) {
        this.publicResults = publicResults;
    }

    // Devuelve el total de votos del jurado.
    public long getTotalJuryVotes() {
        return totalJuryVotes;
    }

    // Actualiza el total de votos del jurado.
    public void setTotalJuryVotes(long totalJuryVotes) {
        this.totalJuryVotes = totalJuryVotes;
    }

    // Devuelve el ranking de ganadores del jurado.
    public List<ResultItemResponse> getJuryResults() {
        return juryResults;
    }

    // Actualiza el ranking de ganadores del jurado.
    public void setJuryResults(List<ResultItemResponse> juryResults) {
        this.juryResults = juryResults;
    }

    public MyTeamResultsResponse getMyTeam() {
        return myTeam;
    }

    public void setMyTeam(MyTeamResultsResponse myTeam) {
        this.myTeam = myTeam;
    }
}
