package com.votify.backend.domain.event;

import com.votify.backend.entity.EventPhase;

// Estado de dominio que define qué acciones permite una fase del evento.
public interface EventState {
    EventPhase phase();
    boolean registrationsOpen();
    boolean publicVotingOpen();
    boolean juryVotingOpen();
    boolean resultsVisible();
    boolean active();
}
