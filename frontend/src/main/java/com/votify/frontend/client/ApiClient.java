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

// Cliente HTTP singleton que implementa las llamadas a la API backend.
public class ApiClient implements VotifyApi {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static String sessionToken; // Contendrá el ID del usuario como String
    private static String sessionEmail;
    private static String adminPassword; // Token/Password admin
    private static ApiClient instance;

    private final HttpClient httpClient;
    private final String baseUrl;

    // Configura el cliente HTTP y la URL base de la API.
    private ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = System.getenv().getOrDefault("VOTIFY_API_BASE", "http://localhost:8080/api");
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
        return authenticate("/auth/login", email, password);
    }

    // Registra un usuario contra el backend.
    public AuthResponse registerUser(String email, String password) {
        return authenticate("/auth/register", email, password);
    }

    @Override
    // Comprueba acceso a inscripción, votación o resultados según estado y sesión.
    public AccessDecision checkAccess(AccessTarget target) {
        EventSettingsResponse settings = getEventSettings();

        return switch (target) {
            case REGISTRATION -> {
                if (!isUserLoggedIn()) {
                    yield AccessDecision.deny("Debes iniciar sesión para registrar un equipo.");
                }
                if (!settings.isRegistrationsOpen()) {
                    yield AccessDecision.deny("Las inscripciones están cerradas actualmente.");
                }
                yield AccessDecision.allow();
            }
            case VOTING -> {
                if (!isUserLoggedIn()) {
                    yield AccessDecision.deny("Debes iniciar sesión para votar.");
                }
                if (!settings.isVotingOpen()) {
                    yield AccessDecision.deny("Las votaciones están cerradas actualmente.");
                }
                if (hasVoted()) {
                    yield AccessDecision.deny("Ya has votado en este evento.");
                }
                yield AccessDecision.allow();
            }
            case RESULTS -> {
                if (!settings.isResultsVisible()) {
                    yield AccessDecision.deny("Los resultados están ocultos actualmente por el administrador.");
                }
                yield AccessDecision.allow();
            }
        };
    }

    // Ejecuta una petición de autenticación y guarda la sesión local si es correcta.
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
            AuthResponse authResponse = MAPPER.readValue(response.body(), AuthResponse.class);
            sessionToken = authResponse.token();
            sessionEmail = authResponse.email();
            return authResponse;
        } catch (Exception e) {
            throw new ApiClientException("No se procesó correctamente la sesión");
        }
    }

    // Envía al backend la creación de un nuevo equipo participante.
    public void createParticipant(String teamName, String email, String phone, String description, String logoBase64, List<String> members) {
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
                .header("X-User-ID", sessionToken == null ? "" : sessionToken)
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
        ParticipantRequest requestBody = new ParticipantRequest(teamName, email, phone, description, logoBase64, members);
        String json;
        try {
            json = MAPPER.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/participants/" + id))
                .header("Content-Type", "application/json")
                .header("X-User-ID", sessionToken == null ? "" : sessionToken)
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

    @Override
    // Obtiene el equipo del usuario actual si existe.
    public ParticipantResponse getCurrentParticipant() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/participants/mine"))
                .header("X-User-ID", sessionToken == null ? "" : sessionToken)
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

    // Crea votos para las selecciones indicadas.
    public VoteResponse createVotes(List<String> selections) {
        if (sessionToken == null) {
            throw new ApiClientException("No has iniciado sesión para poder votar.");
        }

        String json;
        try {
            json = MAPPER.writeValueAsString(new VoteRequest(selections));
        } catch (JsonProcessingException e) {
            throw new ApiClientException("No se pudo preparar la solicitud al servidor");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/votes"))
                .header("Content-Type", "application/json")
                .header("X-User-ID", sessionToken)
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
        return getEventSettings().getMaxTeamsToVote();
    }

    // Obtiene los resultados agregados desde el backend.
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

    // Consulta si el usuario actual ya ha votado.
    public boolean hasVoted() {
        if (sessionToken == null) {
            return false; // Si no ha iniciado sesión, no puede haber votado.
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/votes/has-voted"))
                .header("X-User-ID", sessionToken)
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }

        String body = response.body() == null ? "" : response.body().trim();
        return "true".equalsIgnoreCase(body);
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
        return "Error del servidor (" + status + ")";
    }

    // Codifica valores para incluirlos de forma segura en una URL.
    private String encode(String value) {
        return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    // Devuelve el correo del usuario actualmente autenticado.
    public String getCurrentUserEmail() {
        return sessionEmail;
    }

    // Limpia los datos de sesión local.
    public void logout() {
        sessionToken = null;
        sessionEmail = null;
    }

    // Indica si hay una sesión de usuario guardada localmente.
    private boolean isUserLoggedIn() {
        return sessionToken != null && !sessionToken.isBlank()
                && sessionEmail != null && !sessionEmail.isBlank();
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
            adminPassword = password;
            return true;
        }
        return false;
    }

    @Override
    // Obtiene los ajustes del evento usando credenciales admin.
    public EventSettingsResponse getAdminSettings() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/settings"))
                .header("X-Admin-Password", adminPassword == null ? "" : adminPassword)
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
    // Actualiza los ajustes administrativos del evento.
    public void updateAdminSettings(boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote) {
        String json = String.format("{\"registrationsOpen\":%b, \"votingOpen\":%b, \"resultsVisible\":%b, \"maxTeamsToVote\":%d}", registrationsOpen, votingOpen, resultsVisible, maxTeamsToVote);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/settings"))
                .header("Content-Type", "application/json")
                .header("X-Admin-Password", adminPassword == null ? "" : adminPassword)
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
                .header("X-Admin-Password", adminPassword == null ? "" : adminPassword)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new ApiClientException(extractErrorMessage(response.body(), response.statusCode()));
        }
    }
}
