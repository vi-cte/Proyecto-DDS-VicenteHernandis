package com.votify.backend.entity;

import com.votify.backend.domain.event.EventState;
import com.votify.backend.domain.event.EventStateFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "events")
// Evento de votación con sus ajustes y estado.
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "registrations_open", nullable = false)
    private boolean registrationsOpen;

    @Column(name = "voting_open", nullable = false)
    private boolean votingOpen;

    @Column(name = "results_visible", nullable = false)
    private boolean resultsVisible;

    @Column(name = "max_teams_to_vote", nullable = false)
    private int maxTeamsToVote;

    @Column(name = "jury_enabled", nullable = false)
    private boolean juryEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "jury_voting_mode", nullable = false, length = 40)
    private JuryVotingMode juryVotingMode = JuryVotingMode.SIMPLE;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false, length = 40)
    private EventPhase phase = EventPhase.REGISTRATION_OPEN;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRegistrationsOpen() { return registrationsOpen; }
    public void setRegistrationsOpen(boolean registrationsOpen) { this.registrationsOpen = registrationsOpen; }
    public boolean isVotingOpen() { return votingOpen; }
    public void setVotingOpen(boolean votingOpen) { this.votingOpen = votingOpen; }
    public boolean isResultsVisible() { return resultsVisible; }
    public void setResultsVisible(boolean resultsVisible) { this.resultsVisible = resultsVisible; }
    public int getMaxTeamsToVote() { return maxTeamsToVote; }
    public void setMaxTeamsToVote(int maxTeamsToVote) { this.maxTeamsToVote = maxTeamsToVote; }
    public boolean isJuryEnabled() { return juryEnabled; }
    public void setJuryEnabled(boolean juryEnabled) { this.juryEnabled = juryEnabled; }
    public JuryVotingMode getJuryVotingMode() { return juryVotingMode == null ? JuryVotingMode.SIMPLE : juryVotingMode; }
    public void setJuryVotingMode(JuryVotingMode juryVotingMode) { this.juryVotingMode = juryVotingMode == null ? JuryVotingMode.SIMPLE : juryVotingMode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public EventPhase getPhase() { return phase == null ? inferLegacyPhase() : phase; }
    public void setPhase(EventPhase phase) {
        this.phase = phase == null ? EventPhase.REGISTRATION_OPEN : phase;
        applyState(EventStateFactory.fromPhase(this.phase));
    }
    public boolean isPublicVotingOpen() { return EventStateFactory.fromPhase(getPhase()).publicVotingOpen(); }
    public boolean isJuryVotingOpen() { return EventStateFactory.fromPhase(getPhase()).juryVotingOpen(); }

    public void syncPhaseFromFlags() {
        setPhase(inferLegacyPhase());
    }

    private EventPhase inferLegacyPhase() {
        if (!active) {
            return EventPhase.ARCHIVED;
        }
        if (resultsVisible) {
            return EventPhase.RESULTS_VISIBLE;
        }
        if (votingOpen && juryEnabled) {
            return EventPhase.PUBLIC_AND_JURY_VOTING_OPEN;
        }
        if (votingOpen) {
            return EventPhase.PUBLIC_VOTING_OPEN;
        }
        if (registrationsOpen) {
            return EventPhase.REGISTRATION_OPEN;
        }
        return EventPhase.REGISTRATION_CLOSED;
    }

    private void applyState(EventState state) {
        this.registrationsOpen = state.registrationsOpen();
        this.votingOpen = state.publicVotingOpen() || state.juryVotingOpen();
        this.resultsVisible = state.resultsVisible();
        this.active = state.active();
        if (state.juryVotingOpen()) {
            this.juryEnabled = true;
        }
    }
}
