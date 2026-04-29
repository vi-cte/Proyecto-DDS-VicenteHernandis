package com.votify.backend.domain.vote;

// Representa un voto de dominio con una opción seleccionada.
public abstract class Vote {
    protected String option;

    // Devuelve el rol del votante asociado a este tipo de voto.
    public abstract String voterRole();

    // Devuelve la opción seleccionada en el voto.
    public String getOption() {
        return option;
    }
}
