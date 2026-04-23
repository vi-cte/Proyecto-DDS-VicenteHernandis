package com.votify.frontend.client;

import com.votify.frontend.dto.AuthResponse;
import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.dto.EventSettingsResponse;
import com.votify.frontend.exception.ApiClientException;

import java.util.List;

public class ApiClientProxy implements VotifyApi {
    private static ApiClientProxy instance;
    private final ApiClient realClient;
    private boolean isAuthenticated = false;
    private boolean isAdminAuthenticated = false;
    private String currentUserEmail = "";

    private ApiClientProxy() {
        this.realClient = new ApiClient();
    }

    public static synchronized ApiClientProxy getInstance() {
        if (instance == null) {
            instance = new ApiClientProxy();
        }
        return instance;
    }

    public void login(String email, String password) {
        AuthResponse response = realClient.login(email, password);
        this.currentUserEmail = response.email();
        this.isAuthenticated = true;
    }

    public void register(String email, String password) {
        AuthResponse response = realClient.registerUser(email, password);
        this.currentUserEmail = response.email();
        this.isAuthenticated = true;
    }

    public void logout() {
        this.isAuthenticated = false;
        this.isAdminAuthenticated = false;
        this.currentUserEmail = "";
    }

    @Override
    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    private void checkAccess() {
        if (!isAuthenticated) {
            throw new ApiClientException("Acceso denegado. Debes iniciar sesión para realizar esta acción.");
        }
    }

    @Override
    public void createParticipant(String teamName, String email, String phone, String description, String logoBase64, List<String> members, String ownerEmail) {
        checkAccess(); realClient.createParticipant(teamName, email, phone, description, logoBase64, members, ownerEmail);
    }
    
    @Override
    public void updateParticipant(Long id, String teamName, String email, String phone, String description, String logoBase64, List<String> members, String ownerEmail) {
        checkAccess(); realClient.updateParticipant(id, teamName, email, phone, description, logoBase64, members, ownerEmail);
    }
    
    @Override
    public List<String> getParticipants() { checkAccess(); return realClient.getParticipants(); }
    @Override
    public List<ParticipantResponse> getParticipantResponses() { checkAccess(); return realClient.getParticipantResponses(); }
    @Override
    public boolean teamNameExists(String teamName) { checkAccess(); return realClient.teamNameExists(teamName); }
    @Override
    public VoteResponse createVotes(List<String> selections) { checkAccess(); return realClient.createVotes(selections); }
    @Override
    public int getVotingLimit() { checkAccess(); return realClient.getVotingLimit(); }
    @Override
    public ResultsResponse getResults() { checkAccess(); return realClient.getResults(); }

    @Override
    public boolean authenticateAdmin(String password) {
        boolean isValid = realClient.authenticateAdmin(password);
        if (isValid) {
            this.isAdminAuthenticated = true;
        }
        return isValid;
    }

    private void checkAdminAccess() {
        if (!isAdminAuthenticated) {
            throw new ApiClientException("Acceso denegado. Se requieren permisos de administrador.");
        }
    }

    @Override
    public EventSettingsResponse getAdminSettings() { checkAdminAccess(); return realClient.getAdminSettings(); }

    @Override
    public EventSettingsResponse getEventSettings() { 
        checkAccess(); 
        return realClient.getAdminSettings(); 
    }

    @Override
    public void updateAdminSettings(boolean registrationsOpen, boolean votingOpen, int maxTeamsToVote) {
        checkAdminAccess(); realClient.updateAdminSettings(registrationsOpen, votingOpen, maxTeamsToVote);
    }

    @Override
    public void resetEvent() { checkAdminAccess(); realClient.resetEvent(); }
}