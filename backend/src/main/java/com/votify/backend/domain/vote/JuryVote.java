package com.votify.backend.domain.vote;

// Voto emitido por un miembro del jurado.
public class JuryVote extends Vote {
    // Crea un voto de jurado para la opción indicada.
    public JuryVote(String option) {
        this.option = option;
    }

    @Override
    // Identifica que el voto pertenece al rol jurado.
    public String voterRole() {
        return "JURY";
    }
}
