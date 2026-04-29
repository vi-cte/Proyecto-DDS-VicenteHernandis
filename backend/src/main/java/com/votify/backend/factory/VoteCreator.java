package com.votify.backend.factory;

import com.votify.backend.domain.vote.Vote;
import com.votify.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

// Factory base para crear votos, con validacion comun en orderVote.
public abstract class VoteCreator {
    // Valida la opcion y delega la creacion concreta a createVote.
    public final Vote orderVote(String option) {
        if (option == null || option.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La opcion no puede ser nula o vacia");
        }
        return createVote(option.trim());
    }

    // Crea el tipo concreto de voto definido por cada subclase.
    protected abstract Vote createVote(String option);
}
