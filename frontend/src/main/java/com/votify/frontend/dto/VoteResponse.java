package com.votify.frontend.dto;

import java.util.List;

// DTO de frontend con la respuesta tras registrar votos.
public class VoteResponse {
    private int recordedVotes;
    private List<String> selections;

    // Constructor vacío requerido por Jackson.
    public VoteResponse() {
    }

    // Devuelve el número de votos registrados.
    public int getRecordedVotes() {
        return recordedVotes;
    }

    // Actualiza el número de votos registrados.
    public void setRecordedVotes(int recordedVotes) {
        this.recordedVotes = recordedVotes;
    }

    // Devuelve las selecciones aceptadas por el backend.
    public List<String> getSelections() {
        return selections;
    }

    // Actualiza las selecciones aceptadas por el backend.
    public void setSelections(List<String> selections) {
        this.selections = selections;
    }
}
