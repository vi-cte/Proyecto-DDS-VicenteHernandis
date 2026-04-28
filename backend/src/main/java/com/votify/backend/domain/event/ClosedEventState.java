package com.votify.backend.domain.event;

public final class ClosedEventState implements EventPhaseState {
    @Override
    public boolean registrationsOpen() {
        return false;
    }

    @Override
    public boolean votingOpen() {
        return false;
    }

    @Override
    public String code() {
        return "CLOSED";
    }
}
