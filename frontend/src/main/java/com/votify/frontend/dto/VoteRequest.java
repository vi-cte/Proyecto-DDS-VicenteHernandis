package com.votify.frontend.dto;

import java.util.List;

// DTO de frontend que envía las selecciones de voto.
public class VoteRequest {
    private List<String> selections;
    private String juryWinnerSelection;
    private String juryTechnicalSelection;

    // Constructor vacío requerido por Jackson.
    public VoteRequest() {
    }

    // Crea una petición de voto con sus selecciones.
    public VoteRequest(List<String> selections) {
        this.selections = selections;
    }

    // Crea una petición de voto de jurado con sus dos categorías.
    public VoteRequest(String juryWinnerSelection, String juryTechnicalSelection) {
        this.selections = List.of();
        this.juryWinnerSelection = juryWinnerSelection;
        this.juryTechnicalSelection = juryTechnicalSelection;
    }

    // Devuelve las selecciones votadas.
    public List<String> getSelections() {
        return selections;
    }

    // Actualiza las selecciones votadas.
    public void setSelections(List<String> selections) {
        this.selections = selections;
    }

    // Devuelve la selección del ganador del jurado.
    public String getJuryWinnerSelection() {
        return juryWinnerSelection;
    }

    // Actualiza la selección del ganador del jurado.
    public void setJuryWinnerSelection(String juryWinnerSelection) {
        this.juryWinnerSelection = juryWinnerSelection;
    }

    // Devuelve la selección de mención técnica del jurado.
    public String getJuryTechnicalSelection() {
        return juryTechnicalSelection;
    }

    // Actualiza la selección de mención técnica del jurado.
    public void setJuryTechnicalSelection(String juryTechnicalSelection) {
        this.juryTechnicalSelection = juryTechnicalSelection;
    }
}
