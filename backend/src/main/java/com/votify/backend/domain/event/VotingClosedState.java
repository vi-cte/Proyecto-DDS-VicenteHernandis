package com.votify.backend.domain.event;

import com.votify.backend.entity.EventPhase;

public class VotingClosedState implements EventState {
    public EventPhase phase() { return EventPhase.VOTING_CLOSED; }
    public boolean registrationsOpen() { return false; }
    public boolean publicVotingOpen() { return false; }
    public boolean juryVotingOpen() { return false; }
    public boolean resultsVisible() { return false; }
    public boolean active() { return true; }
}
