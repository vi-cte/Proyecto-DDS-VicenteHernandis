package com.votify.backend.domain.event;

public final class VotingOpenState implements EventPhaseState {
    @Override
    public boolean registrationsOpen() {
        return false;
    }

    @Override
    public boolean votingOpen() {
        return true;
    }

    @Override
    public String code() {
        return "VOTING_OPEN";
    }
}
