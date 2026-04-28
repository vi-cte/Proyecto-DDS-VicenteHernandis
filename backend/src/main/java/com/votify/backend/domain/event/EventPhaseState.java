package com.votify.backend.domain.event;

public interface EventPhaseState {
    boolean registrationsOpen();

    boolean votingOpen();

    String code();
}
