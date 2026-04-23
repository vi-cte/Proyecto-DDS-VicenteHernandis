package com.votify.frontend.client;

import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.VoteResponse;
import java.util.List;

public interface VotifyApi {
    void createParticipant(String teamName, String email, String phone, String description, String logoBase64, List<String> members);
    List<String> getParticipants();
    List<ParticipantResponse> getParticipantResponses();
    boolean teamNameExists(String teamName);
    VoteResponse createVotes(List<String> selections);
    int getVotingLimit();
    ResultsResponse getResults();
    String getCurrentUserEmail();
}