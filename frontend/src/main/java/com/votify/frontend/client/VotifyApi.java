package com.votify.frontend.client;

import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.dto.EventSettingsResponse;
import com.votify.frontend.dto.AdminEventResponse;
import com.votify.frontend.dto.EventResponse;
import com.votify.frontend.dto.JuryCriterionScoreRequest;
import com.votify.frontend.dto.JuryTeamEvaluationRequest;
import com.votify.frontend.dto.VoteSelectionRequest;
import com.votify.frontend.dto.VoteSettingsResponse;
import java.util.List;

// Contrato de comunicación entre el frontend y la API de Votify.
public interface VotifyApi {
    // Comprueba si se puede acceder a una funcionalidad concreta.
    AccessDecision checkAccess(AccessTarget target);
    // Crea un participante con los datos del formulario.
    void createParticipant(String teamName, String email, String phone, String description, String logoBase64, List<String> members);
    void createParticipant(Long eventId, String teamName, String email, String phone, String description, String logoBase64, List<String> members);
    // Actualiza un participante existente con los datos del formulario.
    void updateParticipant(Long id, String teamName, String email, String phone, String description, String logoBase64, List<String> members);
    void updateParticipant(Long eventId, Long id, String teamName, String email, String phone, String description, String logoBase64, List<String> members);
    // Devuelve solo los nombres de los equipos participantes.
    List<String> getParticipants();
    // Devuelve todos los datos de los equipos participantes.
    List<ParticipantResponse> getParticipantResponses();
    List<ParticipantResponse> getParticipantResponses(Long eventId);
    // Devuelve el participante asociado al usuario actual.
    ParticipantResponse getCurrentParticipant();
    ParticipantResponse getCurrentParticipant(Long eventId);
    // Comprueba si un nombre de equipo ya existe.
    boolean teamNameExists(String teamName);
    boolean teamNameExists(Long eventId, String teamName);
    // Envía las selecciones de voto al backend.
    VoteResponse createVotes(List<String> selections);
    VoteResponse createVotes(Long eventId, List<String> selections);
    VoteResponse createVotes(Long eventId, List<VoteSelectionRequest> selections, boolean withComments);
    // Envía las selecciones de voto del jurado al backend.
    VoteResponse createJuryVotes(String winnerSelection, String technicalSelection);
    VoteResponse createJuryVotes(Long eventId, String winnerSelection, String technicalSelection);
    VoteResponse createJuryVotes(Long eventId, String winnerSelection, String winnerComment, String technicalSelection, String technicalComment);
    VoteResponse createJuryMulticriteriaVotes(Long eventId, String teamSelection, List<JuryCriterionScoreRequest> criteriaScores);
    VoteResponse createJuryMulticriteriaVotes(Long eventId, String teamSelection, List<JuryCriterionScoreRequest> criteriaScores, String comment);
    VoteResponse createJuryMulticriteriaVotes(Long eventId, List<JuryTeamEvaluationRequest> evaluations);
    // Devuelve el límite máximo de equipos votables.
    int getVotingLimit();
    int getVotingLimit(Long eventId);
    VoteSettingsResponse getVoteSettings();
    VoteSettingsResponse getVoteSettings(Long eventId);
    // Obtiene el modo de votación del jurado directamente del JSON
    String getJuryVotingModeRaw(Long eventId);
    // Descarga los resultados agregados.
    ResultsResponse getResults();
    ResultsResponse getResults(Long eventId);
    // Indica si el usuario actual ya ha votado.
    boolean hasVoted();
    boolean hasVoted(Long eventId);
    List<String> getEvaluatedJuryTeams(Long eventId);
    // Devuelve el correo del usuario de la sesión actual.
    String getCurrentUserEmail();
    // Devuelve el rol del usuario de la sesión actual.
    String getCurrentUserRole();
    // Indica si el usuario actual es jurado.
    boolean isCurrentUserJury();
    // Valida la contraseña de administrador.
    boolean authenticateAdmin(String password);
    // Crea una cuenta de jurado desde administración.
    void createJuryAccount(String email, String password);
    // Obtiene los ajustes desde el endpoint administrativo.
    EventSettingsResponse getAdminSettings();
    // Obtiene los ajustes públicos del evento.
    EventSettingsResponse getEventSettings();
    // Devuelve eventos seleccionables.
    List<EventResponse> getEvents();
    // Actualiza los ajustes administrativos del evento.
    void updateAdminSettings(boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote, String juryVotingMode);
    // Reinicia votos y participantes del evento.
    void resetEvent();
    // Devuelve eventos administrativos.
    List<AdminEventResponse> getAdminEvents();
    // Crea un evento administrativo.
    AdminEventResponse createAdminEvent(String name, String eventDate, String description, boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote, boolean juryEnabled, String juryVotingMode, String phase);
    // Actualiza un evento administrativo.
    AdminEventResponse updateAdminEvent(Long id, String name, String eventDate, String description, boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote, boolean juryEnabled, String juryVotingMode, String phase);
    // Archiva un evento administrativo.
    void archiveAdminEvent(Long id);
}
