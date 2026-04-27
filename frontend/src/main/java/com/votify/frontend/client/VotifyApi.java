package com.votify.frontend.client;

import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.dto.EventSettingsResponse;
import java.util.List;

public interface VotifyApi {
    void createParticipant(String teamName, String email, String phone, String description, String logoBase64, List<String> members, String ownerEmail);
    void updateParticipant(Long id, String teamName, String email, String phone, String description, String logoBase64, List<String> members, String ownerEmail);
    List<String> getParticipants();
    List<ParticipantResponse> getParticipantResponses();
    boolean teamNameExists(String teamName);
    VoteResponse createVotes(List<String> selections);
    int getVotingLimit();
    ResultsResponse getResults();
    boolean hasVoted();
    String getCurrentUserEmail();
    boolean authenticateAdmin(String password);
    EventSettingsResponse getAdminSettings();
    EventSettingsResponse getEventSettings();
    void updateAdminSettings(boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote);
    void resetEvent();
}