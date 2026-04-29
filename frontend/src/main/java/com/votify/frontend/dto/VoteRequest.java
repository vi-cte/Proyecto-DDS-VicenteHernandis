package com.votify.frontend.dto;

import java.util.List;

// DTO de frontend que envía las selecciones de voto.
public class VoteRequest {
    private List<String> selections;

    // Constructor vacío requerido por Jackson.
    public VoteRequest() {
    }

    // Crea una petición de voto con sus selecciones.
    public VoteRequest(List<String> selections) {
        this.selections = selections;
    }

    // Devuelve las selecciones votadas.
    public List<String> getSelections() {
        return selections;
    }

    // Actualiza las selecciones votadas.
    public void setSelections(List<String> selections) {
        this.selections = selections;
    }
}
