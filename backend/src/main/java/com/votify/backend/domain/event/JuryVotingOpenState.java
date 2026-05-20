package com.votify.backend.domain.event;

import com.votify.backend.entity.EventPhase;

public class JuryVotingOpenState implements EventState {
    public EventPhase phase() { return EventPhase.JURY_VOTING_OPEN; }
    public boolean registrationsOpen() { return false; }
    public boolean publicVotingOpen() { return false; }
    public boolean juryVotingOpen() { return true; }
    public boolean resultsVisible() { return false; }
    public boolean active() { return true; }
}
