package com.votify.backend.domain.event;

import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.entity.EventSettingsEntity;

public final class EventSettingsStateContext {
    private EventPhaseState phaseState;
    private boolean resultsVisible;
    private int maxTeamsToVote;

    private EventSettingsStateContext(EventPhaseState phaseState, boolean resultsVisible, int maxTeamsToVote) {
        this.phaseState = phaseState;
        this.resultsVisible = resultsVisible;
        this.maxTeamsToVote = maxTeamsToVote;
    }

    public static EventSettingsStateContext fromEntity(EventSettingsEntity entity) {
        EventPhaseState phase = entity.getPhase() == null || entity.getPhase().isBlank()
                ? EventPhaseStateFactory.fromFlags(entity.isRegistrationsOpen(), entity.isVotingOpen())
                : EventPhaseStateFactory.fromCode(entity.getPhase());

        return new EventSettingsStateContext(phase, entity.isResultsVisible(), entity.getMaxTeamsToVote());
    }

    public void applyAdminSelection(EventSettingsDto settings) {
        phaseState = EventPhaseStateFactory.fromAdminSelection(settings.registrationsOpen(), settings.votingOpen());
        resultsVisible = settings.resultsVisible();
        maxTeamsToVote = settings.maxTeamsToVote();
    }

    public EventSettingsDto toDto() {
        return new EventSettingsDto(
                phaseState.registrationsOpen(),
                phaseState.votingOpen(),
                resultsVisible,
                maxTeamsToVote
        );
    }

    public void applyTo(EventSettingsEntity entity) {
        entity.setPhase(phaseState.code());
        entity.setRegistrationsOpen(phaseState.registrationsOpen());
        entity.setVotingOpen(phaseState.votingOpen());
        entity.setResultsVisible(resultsVisible);
        entity.setMaxTeamsToVote(maxTeamsToVote);
    }

    public boolean registrationsOpen() {
        return phaseState.registrationsOpen();
    }

    public boolean votingOpen() {
        return phaseState.votingOpen();
    }

    public boolean resultsVisible() {
        return resultsVisible;
    }

    public int maxTeamsToVote() {
        return maxTeamsToVote;
    }
}
