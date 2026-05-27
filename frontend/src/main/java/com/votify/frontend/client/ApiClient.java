package com.votify.frontend.client;

import com.votify.frontend.dto.AuthRequest;
import com.votify.frontend.dto.AuthResponse;
import com.votify.frontend.dto.AdminEventRequest;
import com.votify.frontend.dto.AdminEventResponse;
import com.votify.frontend.dto.JuryCriterionScoreRequest;
import com.votify.frontend.dto.JuryTeamEvaluationRequest;
import com.votify.frontend.dto.ParticipantRequest;
import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.VoteRequest;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.dto.VoteSelectionRequest;
import com.votify.frontend.dto.VoteSettingsResponse;
import com.votify.frontend.exception.ErrorResponse;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.dto.EventSettingsResponse;
import com.votify.frontend.dto.EventResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Cliente HTTP singleton que implementa las llamadas a la API backend.
public class ApiClient implements VotifyApi {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static ApiClient instance;

    private final HttpClient httpClient;
    private final String baseUrl;
    private final SessionManager sessionManager;

    // Configura el cliente HTTP y la URL base de la API.
    private ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = System.getenv().getOrDefault("VOTIFY_API_BASE", "http://localhost:8080/api");
        this.sessionManager = new SessionManager();
    }

    // Devuelve la instancia compartida del cliente API.
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    // Inicia sesión contra el backend.
    public AuthResponse login(String email, String password) {
        return authenticate("/auth/login", email, password, null);
    }

    // Registra un usuario contra el backend.
    public AuthResponse registerUser(String email, String password) {
        return registerUser(email, password, "PUBLIC");
    }

    // Registra un usuario con el rol indicado contra el backend.
    public AuthResponse registerUser(String email, String password, String role) {
        return authenticate("/auth/register", email, password, role);
    }

    @Override
    // Comprueba acceso a inscripción, votación o resultados según estado y sesión.
    public AccessDecision checkAccess(AccessTarget target) {
        List<EventResponse> events = getEvents();

        return switch (target) {
            case REGISTRATION -> {
                if (!isUserLoggedIn()) {
                    yield AccessDecision.deny("Debes iniciar sesión para registrar un equipo.");
                }
                boolean anyOpen = events.stream().anyMatch(e -> e.isActive() && e.isRegistrationsOpen());
                if (!anyOpen) {
                    yield AccessDecision.deny("Las inscripciones están cerradas actualmente.");
                }
                yield AccessDecision.allow();
            }
            case VOTING -> {
                if (!isUserLoggedIn()) {
                    yield AccessDecision.deny("Debes iniciar sesión para votar.");
                }
                boolean anyOpen = false;
                boolean canVote = false;
                for (EventResponse event : events) {
                    if (event.isActive() && canCurrentUserVoteInPhase(event)) {
                        anyOpen = true;
                        if (!hasVoted(event.getId())) {
                            canVote = true;
                            break;
                        }
                    }
                }
                if (!anyOpen) {
                    yield AccessDecision.deny("Las votaciones están cerradas actualmente.");
                }
                if (!canVote) {
                    yield AccessDecision.deny("Ya has votado en todos los eventos disponibles.");
                }
                yield AccessDecision.allow();
            }
            case RESULTS -> {
                boolean anyVisible = events.stream().anyMatch(EventResponse::isResultsVisible);
                if (!anyVisible) {
                    yield AccessDecision.deny("Los resultados están ocultos actualmente por el administrador.");
                }
                yield AccessDecision.allow();
            }
        };
    }

    // Ejecuta una petición de autenticación y guarda la sesión local si es correcta.
    private AuthResponse authenticate(String endpoint, String email, String password, String role) {
        String json;
        try {
            json = MAPPER.writeValueAsString(new AuthRequest(email, password, role));
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
            AuthResponse authResponse = MAPPER.readValue(response.body(), AuthResponse.class);
            sessionManager.startUserSession(authResponse.token(), authResponse.email(), authResponse.role());
            return authResponse;
        } catch (Exception e) {
            throw new ApiClientException("No se procesó correctamente la sesión");
        }
    }

    // Envía al backend la creación de un nuevo equipo participante.
    public void createParticipant(String teamName, String email, String phone, String description, String logoBase64, List<String> members) {
        createParticipant(null, teamName, email, phone, description, logoBase64, members);
    }

    @Override
    // Envía al backend la creación de un nuevo equipo participante para un evento.
    public void createParticipant(Long eventId, String teamName, String email, String phone, String description, String logoBase64, List<String> members) {
        ParticipantRequest requestBody = new ParticipantRequest(teamName, email, phone, description, logoBase64, members);
        String json;
        try {
            json = MAPPER.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/participants"))
                .header("Content-Type", "application/json")
                .header("X-User-ID", sessionManager.userIdHeaderValue())
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
    // Envía al backend la actualización de un equipo participante.
    public void updateParticipant(Long id, String teamName, String email, String phone, String description, String logoBase64, List<String> members) {
        updateParticipant(null, id, teamName, email, phone, description, logoBase64, members);
    }

    @Override
    // Envía al backend la actualización de un equipo participante para un evento.
    public void updateParticipant(Long eventId, Long id, String teamName, String email, String phone, String description, String logoBase64, List<String> members) {
        ParticipantRequest requestBody = new ParticipantRequest(teamName, email, phone, description, logoBase64, members);
        String json;
        try {
            json = MAPPER.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/participants/" + id, eventId)))
                .header("Content-Type", "application/json")
                .header("X-User-ID", sessionManager.userIdHeaderValue())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = send(request);
        int status = response.statusCode();
        if (status == 200 || status == 204) {
            return;
        }
        throw new ApiClientException(extractErrorMessage(response.body(), status));
    }

    // Obtiene los nombres de todos los equipos participantes.
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

    // Obtiene todos los participantes como DTOs completos.
    public List<ParticipantResponse> getParticipantResponses() {
        return getParticipantResponses(null);
    }

    @Override
    // Obtiene todos los participantes del evento indicado como DTOs completos.
    public List<ParticipantResponse> getParticipantResponses(Long eventId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/participants", eventId)))
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

    @Override
    // Obtiene el equipo del usuario actual si existe.
    public ParticipantResponse getCurrentParticipant() {
        return getCurrentParticipant(null);
    }

    @Override
    // Obtiene el equipo del usuario actual en el evento indicado si existe.
    public ParticipantResponse getCurrentParticipant(Long eventId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/participants/mine", eventId)))
                .header("X-User-ID", sessionManager.userIdHeaderValue())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() == 204 || response.statusCode() == 404) {
            return null;
        }
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }

        try {
            return MAPPER.readValue(response.body(), ParticipantResponse.class);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar tu participante");
        }
    }

    // Consulta si el nombre de equipo ya está registrado.
    public boolean teamNameExists(String teamName) {
        return teamNameExists(null, teamName);
    }

    @Override
    // Consulta si el nombre de equipo ya está registrado en un evento.
    public boolean teamNameExists(Long eventId, String teamName) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/participants/exists?teamName=" + encode(teamName) + eventIdParam(eventId, true)))
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        String body = response.body() == null ? "" : response.body().trim();
        return "true".equalsIgnoreCase(body);
    }

    // Crea votos para las selecciones indicadas.
    public VoteResponse createVotes(List<String> selections) {
        return createVotes(null, selections);
    }

    @Override
    // Crea votos para las selecciones indicadas en un evento.
    public VoteResponse createVotes(Long eventId, List<String> selections) {
        return createVotes(eventId, selections == null ? List.of() : selections.stream()
                .map(selection -> new VoteSelectionRequest(selection, null))
                .toList(), true);
    }

    @Override
    // Crea votos públicos incluyendo comentarios opcionales por equipo.
    public VoteResponse createVotes(Long eventId, List<VoteSelectionRequest> selections, boolean withComments) {
        if (!sessionManager.hasUserToken()) {
            throw new ApiClientException("No has iniciado sesión para poder votar.");
        }

        String json;
        try {
            json = MAPPER.writeValueAsString(new VoteRequest(selections, true));
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/votes", eventId)))
                .header("Content-Type", "application/json")
                .header("X-User-ID", sessionManager.userIdHeaderValue())
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

    // Obtiene el límite de votos por usuario.
    public int getVotingLimit() {
        return getVotingLimit(null);
    }

    @Override
    // Obtiene el límite de votos por usuario en un evento.
    public int getVotingLimit(Long eventId) {
        return getVoteSettings(eventId).getMaxTeamsToVote();
    }

    @Override
    // Obtiene la configuracion completa de la votacion.
    public VoteSettingsResponse getVoteSettings() {
        return getVoteSettings(null);
    }

    @Override
    // Obtiene la configuracion completa de la votacion en un evento.
    public VoteSettingsResponse getVoteSettings(Long eventId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/votes/settings", eventId)))
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        try {
            return MAPPER.readValue(response.body(), VoteSettingsResponse.class);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar la configuración de votación");
        }
    }

    // Obtiene el modo de votación del jurado directamente del JSON para evitar fallos de mapeo DTO.
    public String getJuryVotingModeRaw(Long eventId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/votes/settings", eventId)))
                .GET()
                .build();
        HttpResponse<String> response = send(request);
        try {
            com.fasterxml.jackson.databind.JsonNode node = MAPPER.readTree(response.body());
            if (node.has("juryVotingMode") && !node.get("juryVotingMode").isNull()) {
                return node.get("juryVotingMode").asText();
            }
        } catch (Exception ignored) {}
        return "SIMPLE";
    }

    @Override
    // Crea los dos votos de un usuario jurado.
    public VoteResponse createJuryVotes(String winnerSelection, String technicalSelection) {
        return createJuryVotes(null, winnerSelection, technicalSelection);
    }

    @Override
    // Crea los dos votos de un usuario jurado en un evento.
    public VoteResponse createJuryVotes(Long eventId, String winnerSelection, String technicalSelection) {
        return createJuryVotes(eventId, winnerSelection, null, technicalSelection, null);
    }

    @Override
    // Crea los dos votos simples del jurado con comentarios opcionales.
    public VoteResponse createJuryVotes(Long eventId, String winnerSelection, String winnerComment, String technicalSelection, String technicalComment) {
        if (!sessionManager.hasUserToken()) {
            throw new ApiClientException("No has iniciado sesión para poder votar.");
        }

        String json;
        try {
            VoteRequest request = new VoteRequest(winnerSelection, technicalSelection);
            request.setJuryWinnerComment(winnerComment);
            request.setJuryTechnicalComment(technicalComment);
            json = MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/votes", eventId)))
                .header("Content-Type", "application/json")
                .header("X-User-ID", sessionManager.userIdHeaderValue())
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

    @Override
    // Crea una evaluacion multicriterio del jurado.
    public VoteResponse createJuryMulticriteriaVotes(Long eventId, String teamSelection, List<JuryCriterionScoreRequest> criteriaScores) {
        return createJuryMulticriteriaVotes(eventId, teamSelection, criteriaScores, null);
    }

    @Override
    // Crea una evaluacion multicriterio del jurado con comentario opcional.
    public VoteResponse createJuryMulticriteriaVotes(Long eventId, String teamSelection, List<JuryCriterionScoreRequest> criteriaScores, String comment) {
        return createJuryMulticriteriaVotes(eventId, List.of(new JuryTeamEvaluationRequest(teamSelection, criteriaScores, comment)));
    }

    @Override
    // Crea una evaluacion multicriterio del jurado para todos los equipos en una sola peticion.
    public VoteResponse createJuryMulticriteriaVotes(Long eventId, List<JuryTeamEvaluationRequest> evaluations) {
        if (!sessionManager.hasUserToken()) {
            throw new ApiClientException("No has iniciado sesión para poder votar.");
        }

        String json;
        try {
            VoteRequest body = new VoteRequest();
            body.setSelections(List.of());
            body.setSelectionEntries(List.of());
            body.setJuryCriteriaScores(List.of());
            body.setJuryTeamEvaluations(evaluations);
            json = MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/votes", eventId)))
                .header("Content-Type", "application/json")
                .header("X-User-ID", sessionManager.userIdHeaderValue())
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

    // Obtiene los resultados agregados desde el backend.
    public ResultsResponse getResults() {
        return getResults(null);
    }

    @Override
    // Obtiene los resultados agregados desde el backend para un evento.
    public ResultsResponse getResults(Long eventId) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/results", eventId)))
                .GET();
        if (sessionManager.hasUserToken()) {
            builder.header("X-User-ID", sessionManager.userIdHeaderValue());
        }
        if (sessionManager.hasAdminSession()) {
            builder.header("X-Admin-Password", sessionManager.adminPasswordHeaderValue());
        }
        HttpRequest request = builder.build();

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

    // Consulta si el usuario actual ya ha votado.
    public boolean hasVoted() {
        return hasVoted(null);
    }

    @Override
    // Consulta si el usuario actual ya ha votado en un evento.
    public boolean hasVoted(Long eventId) {
        if (!sessionManager.hasUserToken()) {
            return false; // Si no ha iniciado sesión, no puede haber votado.
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/votes/has-voted", eventId)))
                .header("X-User-ID", sessionManager.userIdHeaderValue())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }

        String body = response.body() == null ? "" : response.body().trim();
        return "true".equalsIgnoreCase(body);
    }

    @Override
    // Devuelve los equipos que el jurado ya evaluó en el evento seleccionado.
    public List<String> getEvaluatedJuryTeams(Long eventId) {
        if (!sessionManager.hasUserToken()) {
            return List.of();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(withEventId(baseUrl + "/votes/evaluated-teams", eventId)))
                .header("X-User-ID", sessionManager.userIdHeaderValue())
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }

        try {
            String[] teamNames = MAPPER.readValue(response.body(), String[].class);
            return List.of(teamNames);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar la lista de equipos evaluados");
        }
    }

    // Envía una petición HTTP y convierte errores en ApiClientException.
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

    // Extrae un mensaje legible desde el cuerpo de error JSON.
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
        try {
            AuthResponse authResponse = MAPPER.readValue(body, AuthResponse.class);
            if (authResponse.message() != null && !authResponse.message().isBlank()) {
                return authResponse.message();
            }
        } catch (Exception ignored) {
        }
        return "Error del servidor (" + status + ")";
    }

    // Codifica valores para incluirlos de forma segura en una URL.
    private String encode(String value) {
        return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    }

    // Añade eventId a una URL cuando la pantalla trabaja sobre un evento seleccionado.
    private String withEventId(String url, Long eventId) {
        return eventId == null ? url : url + "?eventId=" + eventId;
    }

    // Añade eventId a una URL que ya tiene otros parametros.
    private String eventIdParam(Long eventId, boolean alreadyHasQuery) {
        if (eventId == null) {
            return "";
        }
        return (alreadyHasQuery ? "&" : "?") + "eventId=" + eventId;
    }

    @Override
    // Valida la contraseña de administrador y la conserva para llamadas admin.
    public boolean authenticateAdmin(String password) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/auth"))
                .header("X-Admin-Password", password)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() == 200) {
            sessionManager.startAdminSession(password);
            return true;
        }
        return false;
    }

    @Override
    // Crea una cuenta de jurado usando la sesión administrativa.
    public void createJuryAccount(String email, String password) {
        String json;
        try {
            json = MAPPER.writeValueAsString(new AuthRequest(email, password, "JURY"));
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la cuenta de jurado");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/jury"))
                .header("Content-Type", "application/json")
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }

    @Override
    // Obtiene los ajustes del evento usando credenciales admin.
    public EventSettingsResponse getAdminSettings() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/settings"))
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
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
    // Obtiene los ajustes públicos del evento.
    public EventSettingsResponse getEventSettings() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/event/settings"))
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
    // Obtiene eventos seleccionables.
    public List<EventResponse> getEvents() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/event"))
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }

        try {
            EventResponse[] events = MAPPER.readValue(response.body(), EventResponse[].class);
            return List.of(events);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar la lista de eventos");
        }
    }

    @Override
    // Actualiza los ajustes administrativos del evento.
    public void updateAdminSettings(boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote, String juryVotingMode) {
        String mode = juryVotingMode == null || juryVotingMode.isBlank() ? "SIMPLE" : juryVotingMode;
        String json = String.format("{\"registrationsOpen\":%b, \"votingOpen\":%b, \"resultsVisible\":%b, \"maxTeamsToVote\":%d, \"juryVotingMode\":\"%s\"}", registrationsOpen, votingOpen, resultsVisible, maxTeamsToVote, mode);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/settings"))
                .header("Content-Type", "application/json")
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }

    @Override
    // Reinicia votos y participantes desde el endpoint admin.
    public void resetEvent() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/reset"))
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }

    @Override
    // Obtiene los eventos del dashboard administrativo.
    public List<AdminEventResponse> getAdminEvents() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/events"))
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .GET()
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        try {
            AdminEventResponse[] events = MAPPER.readValue(response.body(), AdminEventResponse[].class);
            return List.of(events);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar la lista de eventos");
        }
    }

    // Se suscribe al stream SSE de cambios de votos para refrescar el dashboard admin.
    public AutoCloseable subscribeAdminDashboardUpdates(Runnable onDashboardUpdate) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/events/stream"))
                .header("Accept", "text/event-stream")
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .GET()
                .build();

        // La suscripción se devuelve como AutoCloseable para que el controlador la cierre al cambiar de pantalla.
        DashboardUpdateSubscription subscription = new DashboardUpdateSubscription(onDashboardUpdate);
        subscription.start(httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()));
        return subscription;
    }

    @Override
    // Crea un nuevo evento desde administración.
    public AdminEventResponse createAdminEvent(String name, String eventDate, String description, boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote, boolean juryEnabled, String juryVotingMode, String phase) {
        String json;
        try {
            json = MAPPER.writeValueAsString(new AdminEventRequest(name, eventDate, description, registrationsOpen, votingOpen, resultsVisible, maxTeamsToVote, juryEnabled, juryVotingMode, phase));
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar el evento");
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/events"))
                .header("Content-Type", "application/json")
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        try {
            return MAPPER.readValue(response.body(), AdminEventResponse.class);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar el evento creado");
        }
    }

    @Override
    // Actualiza un evento desde administración.
    public AdminEventResponse updateAdminEvent(Long id, String name, String eventDate, String description, boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote, boolean juryEnabled, String juryVotingMode, String phase) {
        String json;
        try {
            json = MAPPER.writeValueAsString(new AdminEventRequest(name, eventDate, description, registrationsOpen, votingOpen, resultsVisible, maxTeamsToVote, juryEnabled, juryVotingMode, phase));
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar el evento");
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/events/" + id))
                .header("Content-Type", "application/json")
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
        try {
            return MAPPER.readValue(response.body(), AdminEventResponse.class);
        } catch (Exception e) {
            throw new ApiClientException("No se pudo procesar el evento actualizado");
        }
    }

    // Elimina un evento desde administración.
    public void deleteAdminEvent(Long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/events/" + id))
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .DELETE()
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }

    @Override
    // Archiva un evento desde administración.
    public void archiveAdminEvent(Long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/events/" + id + "/archive"))
                .header("X-Admin-Password", sessionManager.adminPasswordHeaderValue())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }

    @Override
    // Devuelve el correo del usuario actualmente autenticado.
    public String getCurrentUserEmail() {
        return sessionManager.getCurrentUserEmail();
    }

    @Override
    // Devuelve el rol del usuario actualmente autenticado.
    public String getCurrentUserRole() {
        return sessionManager.getCurrentUserRole();
    }

    @Override
    // Indica si el usuario actualmente autenticado pertenece al jurado.
    public boolean isCurrentUserJury() {
        return sessionManager.isCurrentUserJury();
    }

    private boolean canCurrentUserVoteInPhase(EventResponse event) {
        String phase = event.getPhase() == null ? "" : event.getPhase().trim().toUpperCase(java.util.Locale.ROOT);
        if (isCurrentUserJury()) {
            return event.isJuryEnabled()
                    && ("JURY_VOTING_OPEN".equals(phase) || "PUBLIC_AND_JURY_VOTING_OPEN".equals(phase));
        }
        return "PUBLIC_VOTING_OPEN".equals(phase) || "PUBLIC_AND_JURY_VOTING_OPEN".equals(phase);
    }

    // Mantiene viva la conexión SSE y traduce eventos del backend en callbacks JavaFX.
    private static class DashboardUpdateSubscription implements AutoCloseable {
        private final Runnable onDashboardUpdate;
        // Future de la petición HTTP asíncrona para poder cancelarla si se abandona la pantalla.
        private CompletableFuture<HttpResponse<InputStream>> responseFuture;
        // Stream abierto por el backend mientras la suscripción SSE siga activa.
        private InputStream stream;
        // Bandera compartida con el hilo lector para detenerlo de forma cooperativa.
        private volatile boolean closed;

        DashboardUpdateSubscription(Runnable onDashboardUpdate) {
            this.onDashboardUpdate = onDashboardUpdate;
        }

        void start(CompletableFuture<HttpResponse<InputStream>> future) {
            this.responseFuture = future;
            future.thenAccept(response -> {
                if (closed || response.statusCode() != 200) {
                    return;
                }
                stream = response.body();
                // El lector vive en un hilo daemon para no bloquear el cierre de la app JavaFX.
                Thread readerThread = new Thread(() -> readEvents(stream), "votify-admin-dashboard-sse");
                readerThread.setDaemon(true);
                readerThread.start();
            });
        }

        // Lee el formato SSE línea a línea y dispara el refresco solo para eventos de actualización.
        private void readEvents(InputStream inputStream) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while (!closed && (line = reader.readLine()) != null) {
                    if (isDashboardUpdateEvent(line)) {
                        onDashboardUpdate.run();
                    }
                }
            } catch (IOException ignored) {
            }
        }

        private boolean isDashboardUpdateEvent(String line) {
            if (!line.startsWith("event:")) {
                return false;
            }
            return "dashboard-updated".equals(line.substring("event:".length()).trim());
        }

        @Override
        // Cierra la suscripción cuando el dashboard deja de estar visible.
        public void close() {
            closed = true;
            if (responseFuture != null) {
                responseFuture.cancel(true);
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    // Limpia los datos de sesión local.
    public void logout() {
        sessionManager.clearUserSession();
    }

    // Indica si hay una sesión de usuario guardada localmente.
    private boolean isUserLoggedIn() {
        return sessionManager.isUserLoggedIn();
    }
}
