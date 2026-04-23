package com.votify.frontend.client;

import com.votify.frontend.dto.AuthRequest;
import com.votify.frontend.dto.AuthResponse;
import com.votify.frontend.dto.ParticipantRequest;
import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.VoteRequest;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.exception.ErrorResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.dto.EventSettingsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ApiClient implements VotifyApi {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final HttpClient httpClient;
    private final String baseUrl;

    public ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = System.getenv().getOrDefault("VOTIFY_API_BASE", "http://localhost:8080/api");
    }

    public AuthResponse login(String email, String password) {
        return authenticate("/auth/login", email, password);
    }

    public AuthResponse registerUser(String email, String password) {
        return authenticate("/auth/register", email, password);
    }

    private AuthResponse authenticate(String endpoint, String email, String password) {
        String json;
        try {
            json = MAPPER.writeValueAsString(new AuthRequest(email, password));
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud de autenticación");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = send(request);
        
        if (response.statusCode() == 401 || response.statusCode() == 400) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        if (response.statusCode() != 200) {
            throw new ApiClientException("Error del servidor al intentar autenticar");
        }

        try {
            return MAPPER.readValue(response.body(), AuthResponse.class);
        } catch (Exception e) {
            throw new ApiClientException("No se procesó correctamente la sesión");
        }
    }

    public void createParticipant(String teamName, String email, String phone, String description, String logoBase64, List<String> members, String ownerEmail) {
        ParticipantRequest requestBody = new ParticipantRequest(teamName, email, phone, description, logoBase64, members, ownerEmail);
        String json;
        try {
            json = MAPPER.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/participants"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = send(request);
        int status = response.statusCode();
        if (status == 200 || status == 201) {
            return;
        }
        throw new ApiClientException(extractErrorMessage(response.body(), status));
    }

    @Override
    public void updateParticipant(Long id, String teamName, String email, String phone, String description, String logoBase64, List<String> members, String ownerEmail) {
        ParticipantRequest requestBody = new ParticipantRequest(teamName, email, phone, description, logoBase64, members, ownerEmail);
        String json;
        try {
            json = MAPPER.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/participants/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = send(request);
        int status = response.statusCode();
        if (status == 200 || status == 204) {
            return;
        }
        throw new ApiClientException(extractErrorMessage(response.body(), status));
    }

    public List<String> getParticipants() {
        List<ParticipantResponse> participants = getParticipantResponses();
        List<String> names = new ArrayList<>();
        for (ParticipantResponse participant : participants) {
            if (participant.getTeamName() != null && !participant.getTeamName().isBlank()) {
                names.add(participant.getTeamName());
            }
        }
        return names;
    }

    public List<ParticipantResponse> getParticipantResponses() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/participants"))
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }

        try {
            ParticipantResponse[] participants = MAPPER.readValue(response.body(), ParticipantResponse[].class);
            return List.of(participants);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar la respuesta de participantes");
        }
    }

    public boolean teamNameExists(String teamName) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/participants/exists?teamName=" + encode(teamName)))
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        String body = response.body() == null ? "" : response.body().trim();
        return "true".equalsIgnoreCase(body);
    }

    public VoteResponse createVotes(List<String> selections) {
        String json;
        try {
            json = MAPPER.writeValueAsString(new VoteRequest(selections));
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/votes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }

        try {
            return MAPPER.readValue(response.body(), VoteResponse.class);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar la respuesta del voto");
        }
    }

    public int getVotingLimit() {
        return getAdminSettings().getMaxTeamsToVote();
    }

    public ResultsResponse getResults() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/results"))
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        try {
            return MAPPER.readValue(response.body(), ResultsResponse.class);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar la respuesta de resultados");
        }
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiClientException("No se pudo conectar con el backend. Verifica que este iniciado.");
        } catch (IOException e) {
            throw new ApiClientException("No se pudo conectar con el backend. Verifica que este iniciado.");
        }
    }

    private String extractErrorMessage(String body, int status) {
        if (body == null || body.isBlank()) {
            return "Error del servidor (" + status + ")";
        }
        try {
            ErrorResponse errorResponse = MAPPER.readValue(body, ErrorResponse.class);
            if (errorResponse.getMessage() != null && !errorResponse.getMessage().isBlank()) {
                return errorResponse.getMessage();
            }
        } catch (Exception ignored) {
        }
        return "Error del servidor (" + status + ")";
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public String getCurrentUserEmail() {
        return "";
    }

    @Override
    public boolean authenticateAdmin(String password) {
        return "admin".equals(password);
    }

    @Override
    public EventSettingsResponse getAdminSettings() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/settings"))
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }

        try {
            return MAPPER.readValue(response.body(), EventSettingsResponse.class);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar la configuración");
        }
    }

    @Override
    public EventSettingsResponse getEventSettings() {
        return getAdminSettings();
    }

    @Override
    public void updateAdminSettings(boolean registrationsOpen, boolean votingOpen, int maxTeamsToVote) {
        String json = String.format("{\"registrationsOpen\":%b, \"votingOpen\":%b, \"maxTeamsToVote\":%d}", registrationsOpen, votingOpen, maxTeamsToVote);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/settings"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }

    @Override
    public void resetEvent() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/reset"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }
}
