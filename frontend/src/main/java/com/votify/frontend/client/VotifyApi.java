package com.votify.frontend.client;

import com.votify.frontend.dto.ParticipantResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.VoteResponse;
import com.votify.frontend.dto.EventSettingsResponse;
import java.util.List;

// Contrato de comunicación entre el frontend y la API de Votify.
public interface VotifyApi {
    // Comprueba si se puede acceder a una funcionalidad concreta.
    AccessDecision checkAccess(AccessTarget target);
    // Crea un participante con los datos del formulario.
    void createParticipant(String teamName, String email, String phone, String description, String logoBase64, List<String> members);
    // Actualiza un participante existente con los datos del formulario.
    void updateParticipant(Long id, String teamName, String email, String phone, String description, String logoBase64, List<String> members);
    // Devuelve solo los nombres de los equipos participantes.
    List<String> getParticipants();
    // Devuelve todos los datos de los equipos participantes.
    List<ParticipantResponse> getParticipantResponses();
    // Devuelve el participante asociado al usuario actual.
    ParticipantResponse getCurrentParticipant();
    // Comprueba si un nombre de equipo ya existe.
    boolean teamNameExists(String teamName);
    // Envía las selecciones de voto al backend.
    VoteResponse createVotes(List<String> selections);
    // Envía las selecciones de voto del jurado al backend.
    VoteResponse createJuryVotes(String winnerSelection, String technicalSelection);
    // Devuelve el límite máximo de equipos votables.
    int getVotingLimit();
    // Descarga los resultados agregados.
    ResultsResponse getResults();
    // Indica si el usuario actual ya ha votado.
    boolean hasVoted();
    // Devuelve el correo del usuario de la sesión actual.
    String getCurrentUserEmail();
    // Devuelve el rol del usuario de la sesión actual.
    String getCurrentUserRole();
    // Indica si el usuario actual es jurado.
    boolean isCurrentUserJury();
    // Valida la contraseña de administrador.
    boolean authenticateAdmin(String password);
    // Obtiene los ajustes desde el endpoint administrativo.
    EventSettingsResponse getAdminSettings();
    // Obtiene los ajustes públicos del evento.
    EventSettingsResponse getEventSettings();
    // Actualiza los ajustes administrativos del evento.
    void updateAdminSettings(boolean registrationsOpen, boolean votingOpen, boolean resultsVisible, int maxTeamsToVote);
    // Reinicia votos y participantes del evento.
    void resetEvent();
}
