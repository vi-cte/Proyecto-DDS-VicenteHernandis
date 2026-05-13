package com.votify.backend.factory;

import com.votify.backend.domain.vote.JuryVote;
import com.votify.backend.domain.vote.Vote;
import org.springframework.stereotype.Component;

// Factory concreta que crea votos de jurado.
@Component
public class JuryVoteCreator extends VoteCreator {
    @Override
    // Devuelve una instancia de voto de jurado para la opción recibida.
    protected Vote createVote(String option) {
        return new JuryVote(option);
    }
}
