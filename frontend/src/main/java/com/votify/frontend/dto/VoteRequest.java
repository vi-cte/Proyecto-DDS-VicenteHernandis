package com.votify.frontend.dto;

import java.util.List;

// DTO de frontend que envía las selecciones de voto.
public class VoteRequest {
    private List<String> selections;
    private List<VoteSelectionRequest> selectionEntries;
    private String juryWinnerSelection;
    private String juryTechnicalSelection;
    private String juryWinnerComment;
    private String juryTechnicalComment;
    private String juryTeamSelection;
    private List<JuryCriterionScoreRequest> juryCriteriaScores;
    private String juryTeamComment;

    // Constructor vacío requerido por Jackson.
    public VoteRequest() {
    }

    // Crea una petición de voto con sus selecciones.
    public VoteRequest(List<String> selections) {
        this.selections = selections;
        this.selectionEntries = List.of();
        this.juryCriteriaScores = List.of();
    }

    // Crea una petición pública con comentarios por equipo.
    public VoteRequest(List<VoteSelectionRequest> selectionEntries, boolean withComments) {
        this.selections = List.of();
        this.selectionEntries = selectionEntries;
        this.juryCriteriaScores = List.of();
    }

    // Crea una petición de voto de jurado con sus dos categorías.
    public VoteRequest(String juryWinnerSelection, String juryTechnicalSelection) {
        this.selections = List.of();
        this.selectionEntries = List.of();
        this.juryWinnerSelection = juryWinnerSelection;
        this.juryTechnicalSelection = juryTechnicalSelection;
        this.juryCriteriaScores = List.of();
    }

    // Crea una peticion de jurado multicriterio para un equipo concreto.
    public VoteRequest(String juryTeamSelection, List<JuryCriterionScoreRequest> juryCriteriaScores, String juryTeamComment) {
        this.selections = List.of();
        this.selectionEntries = List.of();
        this.juryTeamSelection = juryTeamSelection;
        this.juryCriteriaScores = juryCriteriaScores;
        this.juryTeamComment = juryTeamComment;
    }

    // Devuelve las selecciones votadas.
    public List<String> getSelections() {
        return selections;
    }

    // Actualiza las selecciones votadas.
    public void setSelections(List<String> selections) {
        this.selections = selections;
    }

    public List<VoteSelectionRequest> getSelectionEntries() {
        return selectionEntries;
    }

    public void setSelectionEntries(List<VoteSelectionRequest> selectionEntries) {
        this.selectionEntries = selectionEntries;
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

    public String getJuryWinnerComment() {
        return juryWinnerComment;
    }

    public void setJuryWinnerComment(String juryWinnerComment) {
        this.juryWinnerComment = juryWinnerComment;
    }

    public String getJuryTechnicalComment() {
        return juryTechnicalComment;
    }

    public void setJuryTechnicalComment(String juryTechnicalComment) {
        this.juryTechnicalComment = juryTechnicalComment;
    }

    public String getJuryTeamSelection() {
        return juryTeamSelection;
    }

    public void setJuryTeamSelection(String juryTeamSelection) {
        this.juryTeamSelection = juryTeamSelection;
    }

    public List<JuryCriterionScoreRequest> getJuryCriteriaScores() {
        return juryCriteriaScores;
    }

    public void setJuryCriteriaScores(List<JuryCriterionScoreRequest> juryCriteriaScores) {
        this.juryCriteriaScores = juryCriteriaScores;
    }

    public String getJuryTeamComment() {
        return juryTeamComment;
    }

    public void setJuryTeamComment(String juryTeamComment) {
        this.juryTeamComment = juryTeamComment;
    }
}
