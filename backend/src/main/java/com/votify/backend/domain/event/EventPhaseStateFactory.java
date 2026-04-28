package com.votify.backend.domain.event;

public final class EventPhaseStateFactory {
    private EventPhaseStateFactory() {
    }

    public static EventPhaseState fromCode(String code) {
        if ("VOTING_OPEN".equalsIgnoreCase(code)) {
            return new VotingOpenState();
        }
        if ("CLOSED".equalsIgnoreCase(code)) {
            return new ClosedEventState();
        }
        return new RegistrationOpenState();
    }

    public static EventPhaseState fromFlags(boolean registrationsOpen, boolean votingOpen) {
        if (registrationsOpen) {
            return new RegistrationOpenState();
        }
        if (votingOpen) {
            return new VotingOpenState();
        }
        return new ClosedEventState();
    }

    public static EventPhaseState fromAdminSelection(boolean registrationsOpen, boolean votingOpen) {
        if (registrationsOpen) {
            return new RegistrationOpenState();
        }
        if (votingOpen) {
            return new VotingOpenState();
        }
        return new ClosedEventState();
    }
}
