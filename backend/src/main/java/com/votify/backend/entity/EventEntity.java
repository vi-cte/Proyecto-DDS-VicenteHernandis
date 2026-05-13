package com.votify.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "active", nullable = false)
    private boolean active;

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
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
