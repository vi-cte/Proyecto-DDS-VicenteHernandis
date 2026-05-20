package com.votify.backend.domain.event;

import com.votify.backend.entity.EventPhase;

// Crea objetos de estado a partir de la fase persistida.
public final class EventStateFactory {
    private EventStateFactory() {
    }

    public static EventState fromPhase(EventPhase phase) {
        return switch (phase == null ? EventPhase.REGISTRATION_OPEN : phase) {
            case REGISTRATION_OPEN -> new RegistrationOpenState();
            case REGISTRATION_CLOSED -> new RegistrationClosedState();
            case PUBLIC_VOTING_OPEN -> new PublicVotingOpenState();
            case JURY_VOTING_OPEN -> new JuryVotingOpenState();
            case VOTING_CLOSED -> new VotingClosedState();
            case RESULTS_VISIBLE -> new ResultsVisibleState();
            case ARCHIVED -> new ArchivedState();
        };
    }
}
