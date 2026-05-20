package com.votify.frontend.dto;

import java.util.List;

// Resumen de evento para el dashboard administrativo.
public class AdminEventResponse {
    private Long id;
    private String name;
    private String eventDate;
    private String description;
    private boolean registrationsOpen;
    private boolean votingOpen;
    private boolean resultsVisible;
    private int maxTeamsToVote;
    private boolean juryEnabled;
    private String juryVotingMode;
    private String phase;
    private boolean active;
    private long totalVotes;
    private long participants;
    private List<ResultItemResponse> ranking;
    private List<ResultItemResponse> publicRanking;
    private List<ResultItemResponse> juryRanking;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
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
    public String getJuryVotingMode() { return juryVotingMode; }
    public void setJuryVotingMode(String juryVotingMode) { this.juryVotingMode = juryVotingMode; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getTotalVotes() { return totalVotes; }
    public void setTotalVotes(long totalVotes) { this.totalVotes = totalVotes; }
    public long getParticipants() { return participants; }
    public void setParticipants(long participants) { this.participants = participants; }
    public List<ResultItemResponse> getRanking() { return ranking; }
    public void setRanking(List<ResultItemResponse> ranking) { this.ranking = ranking; }
    public List<ResultItemResponse> getPublicRanking() { return publicRanking; }
    public void setPublicRanking(List<ResultItemResponse> publicRanking) { this.publicRanking = publicRanking; }
    public List<ResultItemResponse> getJuryRanking() { return juryRanking; }
    public void setJuryRanking(List<ResultItemResponse> juryRanking) { this.juryRanking = juryRanking; }
}
