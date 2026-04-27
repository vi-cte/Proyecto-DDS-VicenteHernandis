package com.votify.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_settings")
public class EventSettingsEntity {
    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(name = "registrations_open", nullable = false)
    private boolean registrationsOpen;

    @Column(name = "voting_open", nullable = false)
    private boolean votingOpen;

    @Column(name = "results_visible", nullable = false)
    private boolean resultsVisible;

    @Column(name = "max_teams_to_vote", nullable = false)
    private int maxTeamsToVote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isRegistrationsOpen() {
        return registrationsOpen;
    }

    public void setRegistrationsOpen(boolean registrationsOpen) {
        this.registrationsOpen = registrationsOpen;
    }

    public boolean isVotingOpen() {
        return votingOpen;
    }

    public void setVotingOpen(boolean votingOpen) {
        this.votingOpen = votingOpen;
    }

    public boolean isResultsVisible() {
        return resultsVisible;
    }

    public void setResultsVisible(boolean resultsVisible) {
        this.resultsVisible = resultsVisible;
    }

    public int getMaxTeamsToVote() {
        return maxTeamsToVote;
    }

    public void setMaxTeamsToVote(int maxTeamsToVote) {
        this.maxTeamsToVote = maxTeamsToVote;
    }
}
