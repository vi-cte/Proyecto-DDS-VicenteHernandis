package com.votify.backend.domain.event;

public final class RegistrationOpenState implements EventPhaseState {
    @Override
    public boolean registrationsOpen() {
        return true;
    }

    @Override
    public boolean votingOpen() {
        return false;
    }

    @Override
    public String code() {
        return "REGISTRATION_OPEN";
    }
}
