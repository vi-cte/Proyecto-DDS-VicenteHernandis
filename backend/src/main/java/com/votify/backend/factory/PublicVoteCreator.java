package com.votify.backend.factory;

import com.votify.backend.domain.vote.PublicVote;
import com.votify.backend.domain.vote.Vote;
import org.springframework.stereotype.Component;

// Factory concreta que crea votos publicos.
@Component
public class PublicVoteCreator extends VoteCreator {
    @Override
    protected Vote createVote(String option) {
        return new PublicVote(option);
    }
}
