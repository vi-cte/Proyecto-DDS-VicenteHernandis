package com.votify.backend.domain.event;

import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.EventPhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventStateTest {

    @Test
    void shouldCreateCorrectStateForEachPhase() {
        assertInstanceOf(RegistrationOpenState.class, EventStateFactory.fromPhase(EventPhase.REGISTRATION_OPEN));
        assertInstanceOf(PublicVotingOpenState.class, EventStateFactory.fromPhase(EventPhase.PUBLIC_VOTING_OPEN));
        assertInstanceOf(JuryVotingOpenState.class, EventStateFactory.fromPhase(EventPhase.JURY_VOTING_OPEN));
        assertInstanceOf(PublicAndJuryVotingOpenState.class, EventStateFactory.fromPhase(EventPhase.PUBLIC_AND_JURY_VOTING_OPEN));
        assertInstanceOf(ResultsVisibleState.class, EventStateFactory.fromPhase(EventPhase.RESULTS_VISIBLE));
        assertInstanceOf(ArchivedState.class, EventStateFactory.fromPhase(EventPhase.ARCHIVED));
    }

    @Test
    void shouldApplyJuryVotingStateToEventFlags() {
        EventEntity event = new EventEntity();

        event.setPhase(EventPhase.JURY_VOTING_OPEN);

        assertFalse(event.isRegistrationsOpen());
        assertFalse(event.isPublicVotingOpen());
        assertTrue(event.isJuryVotingOpen());
        assertFalse(event.isResultsVisible());
        assertTrue(event.isActive());
        assertTrue(event.isJuryEnabled());
    }

    @Test
    void shouldApplyPublicAndJuryVotingStateToEventFlags() {
        EventEntity event = new EventEntity();

        event.setPhase(EventPhase.PUBLIC_AND_JURY_VOTING_OPEN);

        assertFalse(event.isRegistrationsOpen());
        assertTrue(event.isPublicVotingOpen());
        assertTrue(event.isJuryVotingOpen());
        assertFalse(event.isResultsVisible());
        assertTrue(event.isActive());
        assertTrue(event.isJuryEnabled());
    }

    @Test
    void shouldApplyArchivedStateToEventFlags() {
        EventEntity event = new EventEntity();

        event.setPhase(EventPhase.ARCHIVED);

        assertFalse(event.isRegistrationsOpen());
        assertFalse(event.isPublicVotingOpen());
        assertFalse(event.isJuryVotingOpen());
        assertTrue(event.isResultsVisible());
        assertFalse(event.isActive());
    }
}
