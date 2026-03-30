package com.votify.frontend.client;

import com.votify.frontend.dto.ParticipantRequest;
import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.VoteRequest;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.exception.ErrorResponse;
import com.votify.frontend.exception.ApiClientException;
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

public class ApiClient {
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

    public void createParticipant(String teamName, String email, String address, String phone, List<String> members) {
        ParticipantRequest requestBody = new ParticipantRequest(teamName, email, address, phone, members);
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

    public List<String> getParticipants() {
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
            List<String> names = new ArrayList<>();
            for (ParticipantResponse participant : participants) {
                if (participant.getTeamName() != null && !participant.getTeamName().isBlank()) {
                    names.add(participant.getTeamName());
                }
            }
            return names;
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
}
