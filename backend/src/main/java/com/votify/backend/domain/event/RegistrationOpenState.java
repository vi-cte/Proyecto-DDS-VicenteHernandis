package com.votify.backend.domain.event;

import com.votify.backend.entity.EventPhase;

public class RegistrationOpenState implements EventState {
    public EventPhase phase() { return EventPhase.REGISTRATION_OPEN; }
    public boolean registrationsOpen() { return true; }
    public boolean publicVotingOpen() { return false; }
    public boolean juryVotingOpen() { return false; }
    public boolean resultsVisible() { return false; }
    public boolean active() { return true; }
}
