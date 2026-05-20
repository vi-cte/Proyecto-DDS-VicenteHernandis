package com.votify.backend.domain.event;

import com.votify.backend.entity.EventPhase;

public class ResultsVisibleState implements EventState {
    public EventPhase phase() { return EventPhase.RESULTS_VISIBLE; }
    public boolean registrationsOpen() { return false; }
    public boolean publicVotingOpen() { return false; }
    public boolean juryVotingOpen() { return false; }
    public boolean resultsVisible() { return true; }
    public boolean active() { return true; }
}
