package com.votify.backend.domain.event;

import com.votify.backend.entity.EventPhase;

public class PublicVotingOpenState implements EventState {
    public EventPhase phase() { return EventPhase.PUBLIC_VOTING_OPEN; }
    public boolean registrationsOpen() { return false; }
    public boolean publicVotingOpen() { return true; }
    public boolean juryVotingOpen() { return false; }
    public boolean resultsVisible() { return false; }
    public boolean active() { return true; }
}
