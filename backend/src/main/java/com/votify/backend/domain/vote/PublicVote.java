package com.votify.backend.domain.vote;

// Voto emitido por un usuario público de la aplicación.
public class PublicVote extends Vote {
    // Crea un voto público para la opción indicada.
    public PublicVote(String option) {
        this.option = option;
    }

    @Override
    // Identifica que el voto pertenece al rol público.
    public String voterRole() {
        return "PUBLIC";
    }
}
