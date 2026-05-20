package com.votify.backend.domain.event;

import com.votify.backend.entity.EventPhase;

public class ArchivedState implements EventState {
    public EventPhase phase() { return EventPhase.ARCHIVED; }
    public boolean registrationsOpen() { return false; }
    public boolean publicVotingOpen() { return false; }
    public boolean juryVotingOpen() { return false; }
    public boolean resultsVisible() { return true; }
    public boolean active() { return false; }
}
