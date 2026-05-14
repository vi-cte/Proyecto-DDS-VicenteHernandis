package com.votify.backend.service;

import com.votify.backend.domain.vote.Vote;
import com.votify.backend.dto.ResultItemResponse;
import com.votify.backend.dto.ResultsResponse;
import com.votify.backend.dto.JuryCriterionScoreRequest;
import com.votify.backend.dto.MyTeamResultsResponse;
import com.votify.backend.dto.TeamCommentResponse;
import com.votify.backend.dto.VoteRequest;
import com.votify.backend.dto.VoteResponse;
import com.votify.backend.dto.VoteSelectionRequest;
import com.votify.backend.dto.VoteSettingsResponse;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.JuryVotingMode;
import com.votify.backend.entity.User;
import com.votify.backend.entity.UserRole;
import com.votify.backend.entity.VoteCategory;
import com.votify.backend.entity.VoteEntity;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.exception.ApiException;
import com.votify.backend.factory.JuryVoteCreator;
import com.votify.backend.factory.PublicVoteCreator;
import com.votify.backend.repository.UserRepository;
import com.votify.backend.repository.TeamCommentProjection;
import com.votify.backend.repository.VoteJpaRepository;
import com.votify.backend.repository.VoteTallyProjection;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
// Gestiona registro de votos, consulta de resultados y límites de votación.
public class VoteService {
    private static final Map<String, String> JURY_CRITERIA = Map.of(
            "innovacion", "Innovación",
            "viabilidad", "Viabilidad",
            "impacto", "Impacto",
            "presentacion", "Presentación"
    );

    private final VoteJpaRepository voteRepository;
    private final ParticipantService participantService;
    private final PublicVoteCreator voteCreator;
    private final JuryVoteCreator juryVoteCreator;
    private final EventSettingsService eventSettingsService;
    private final UserRepository userRepository;

    // Inyecta repositorios y servicios necesarios para registrar votos.
    public VoteService(
            VoteJpaRepository voteRepository,
            ParticipantService participantService,
            PublicVoteCreator voteCreator,
            JuryVoteCreator juryVoteCreator,
            EventSettingsService eventSettingsService,
            UserRepository userRepository
    ) {
        this.voteRepository = voteRepository;
        this.participantService = participantService;
        this.voteCreator = voteCreator;
        this.juryVoteCreator = juryVoteCreator;
        this.eventSettingsService = eventSettingsService;
        this.userRepository = userRepository;
    }

    @Transactional
    // Registra los votos de un usuario validando límites, duplicados y equipos existentes.
    public VoteResponse createVotes(VoteRequest request, Long userId) {
        return createVotes(request, userId, null);
    }

    @Transactional
    // Registra los votos de un usuario para el evento seleccionado.
    public VoteResponse createVotes(VoteRequest request, Long userId, Long eventId) {
        EventEntity event = eventSettingsService.getEventOrActive(eventId);
        if (!event.isVotingOpen()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Las votaciones estan cerradas actualmente");
        }
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no registrado");
        }
        if (user.getRole() == UserRole.JURY) {
            if (!event.isJuryEnabled()) {
                throw new ApiException(HttpStatus.FORBIDDEN, "La votación del jurado no está habilitada para este evento");
            }
            if (event.getJuryVotingMode() == JuryVotingMode.MULTICRITERIA) {
                return createJuryMulticriteriaVotes(request, userId, event);
            }
            if (voteRepository.existsByEventAndUserId(event, userId)) {
                throw new ApiException(HttpStatus.CONFLICT, "Ya has votado. No puedes votar de nuevo.");
            }
            return createJuryVotes(request, userId, event);
        }
        if (voteRepository.existsByEventAndUserId(event, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya has votado. No puedes votar de nuevo.");
        }
        return createPublicVotes(request, userId, event);
    }

    // Registra votos públicos con el límite configurado por el administrador.
    private VoteResponse createPublicVotes(VoteRequest request, Long userId, EventEntity activeEvent) {
        List<VoteSelectionRequest> selectionEntries = normalizeSelectionEntries(request);
        List<String> normalizedSelections = selectionEntries.stream()
                .map(VoteSelectionRequest::teamName)
                .toList();
        int maxTeamsToVote = activeEvent.getMaxTeamsToVote();
        if (normalizedSelections.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes seleccionar al menos un participante");
        }
        if (normalizedSelections.size() > maxTeamsToVote) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Solo puedes votar a " + maxTeamsToVote + " equipos");
        }
        Set<String> uniqueSelections = new LinkedHashSet<>(normalizedSelections);
        if (uniqueSelections.size() != normalizedSelections.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se permiten opciones duplicadas");
        }

        List<String> orderedSelections = new ArrayList<>(uniqueSelections);
        for (String teamName : orderedSelections) {
            if (!participantService.existsByTeamName(teamName, activeEvent.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El equipo no existe: " + teamName);
            }
        }

        for (String teamName : orderedSelections) {
            Vote vote = voteCreator.orderVote(teamName);
            ParticipantEntity participant = participantService.getByTeamName(vote.getOption(), activeEvent.getId());
            String comment = selectionEntries.stream()
                    .filter(entry -> teamName.equals(entry.teamName()))
                    .map(VoteSelectionRequest::comment)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            voteRepository.save(buildVoteEntity(
                    participant,
                    activeEvent,
                    userId,
                    UserRole.PUBLIC,
                    VoteCategory.PUBLIC_WINNER,
                    null,
                    null,
                    comment
            ));
        }

        return new VoteResponse(orderedSelections.size(), orderedSelections);
    }

    // Registra los dos votos diferenciados del jurado.
    private VoteResponse createJuryVotes(VoteRequest request, Long userId, EventEntity activeEvent) {
        List<String> jurySelections = normalizeSelections(List.of(
                nullToEmpty(request.juryWinnerSelection()),
                nullToEmpty(request.juryTechnicalSelection())
        ));
        if (jurySelections.size() != 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El jurado debe seleccionar un ganador y una mención técnica");
        }

        for (String teamName : jurySelections) {
            if (!participantService.existsByTeamName(teamName, activeEvent.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El equipo no existe: " + teamName);
            }
        }

        Vote winnerVote = juryVoteCreator.orderVote(jurySelections.get(0));
        ParticipantEntity winner = participantService.getByTeamName(winnerVote.getOption(), activeEvent.getId());
        voteRepository.save(buildVoteEntity(
                winner,
                activeEvent,
                userId,
                UserRole.JURY,
                VoteCategory.JURY_WINNER,
                null,
                null,
                trimToNull(request.juryWinnerComment())
        ));

        Vote technicalVote = juryVoteCreator.orderVote(jurySelections.get(1));
        ParticipantEntity technical = participantService.getByTeamName(technicalVote.getOption(), activeEvent.getId());
        voteRepository.save(buildVoteEntity(
                technical,
                activeEvent,
                userId,
                UserRole.JURY,
                VoteCategory.JURY_TECHNICAL,
                null,
                null,
                trimToNull(request.juryTechnicalComment())
        ));

        return new VoteResponse(jurySelections.size(), jurySelections);
    }

    // Registra una evaluacion multicriterio del jurado para un equipo concreto.
    private VoteResponse createJuryMulticriteriaVotes(VoteRequest request, Long userId, EventEntity activeEvent) {
        String teamName = request.juryTeamSelection() == null ? "" : request.juryTeamSelection().trim();
        if (teamName.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes seleccionar un equipo para evaluar");
        }
        if (!participantService.existsByTeamName(teamName, activeEvent.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El equipo no existe: " + teamName);
        }

        ParticipantEntity participant = participantService.getByTeamName(teamName, activeEvent.getId());
        if (voteRepository.existsByEventAndUserIdAndParticipantIdAndVoteCategory(
                activeEvent,
                userId,
                participant.getId(),
                VoteCategory.JURY_MULTICRITERIA
        )) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya has evaluado a este equipo con la votación multicriterio");
        }

        List<JuryCriterionScoreRequest> scores = request.juryCriteriaScores() == null
                ? List.of()
                : request.juryCriteriaScores();
        if (scores.size() != JURY_CRITERIA.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes puntuar todos los criterios del jurado");
        }

        Set<String> receivedCriteria = new HashSet<>();
        String teamComment = trimToNull(request.juryTeamComment());
        boolean commentStored = false;
        for (JuryCriterionScoreRequest scoreRequest : scores) {
            if (scoreRequest == null || scoreRequest.criterion() == null || scoreRequest.score() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cada criterio del jurado debe incluir nombre y puntuación");
            }
            String normalizedCriterion = normalizeCriterion(scoreRequest.criterion());
            if (!JURY_CRITERIA.containsKey(normalizedCriterion)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Criterio del jurado no soportado: " + scoreRequest.criterion());
            }
            if (!receivedCriteria.add(normalizedCriterion)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes repetir criterios en la misma evaluación");
            }
            if (scoreRequest.score() < 0 || scoreRequest.score() > 10) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Las puntuaciones del jurado deben estar entre 0 y 10");
            }
            voteRepository.save(buildVoteEntity(
                    participant,
                    activeEvent,
                    userId,
                    UserRole.JURY,
                    VoteCategory.JURY_MULTICRITERIA,
                    normalizedCriterion,
                    scoreRequest.score(),
                    commentStored ? null : teamComment
            ));
            commentStored = true;
        }

        return new VoteResponse(scores.size(), List.of(teamName));
    }

    @Transactional(readOnly = true)
    // Calcula y devuelve el resumen agregado de resultados.
    public ResultsResponse getResults() {
        return getResults(null, null);
    }

    @Transactional(readOnly = true)
    // Calcula y devuelve el resumen agregado de resultados del evento seleccionado.
    public ResultsResponse getResults(Long eventId) {
        return getResults(eventId, null);
    }

    @Transactional(readOnly = true)
    // Calcula y devuelve el resumen agregado y, si aplica, los comentarios del equipo del usuario autenticado.
    public ResultsResponse getResults(Long eventId, Long userId) {
        EventEntity event = eventSettingsService.getEventOrActive(eventId);
        if (!event.isResultsVisible()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Los resultados estan ocultos actualmente por el administrador");
        }
        List<ResultItemResponse> publicResults = toResultItems(voteRepository.tallyByEventAndVoterRole(event, UserRole.PUBLIC));
        List<ResultItemResponse> juryResults = toResultItems(voteRepository.tallyByEventAndVoterRole(event, UserRole.JURY));
        long totalPublicVotes = voteRepository.countByEventAndVoterRole(event, UserRole.PUBLIC);
        long totalJuryVotes = voteRepository.countByEventAndVoterRole(event, UserRole.JURY);
        return new ResultsResponse(
                totalPublicVotes + totalJuryVotes,
                publicResults,
                totalPublicVotes,
                publicResults,
                totalJuryVotes,
                juryResults,
                buildMyTeamResults(event, userId, publicResults)
        );
    }

    @Transactional(readOnly = true)
    // Devuelve la configuración de votación consumida por el frontend.
    public VoteSettingsResponse getVoteSettings() {
        return getVoteSettings(null);
    }

    @Transactional(readOnly = true)
    // Devuelve la configuración de votación del evento seleccionado.
    public VoteSettingsResponse getVoteSettings(Long eventId) {
        EventEntity event = eventSettingsService.getEventOrActive(eventId);
        return new VoteSettingsResponse(event.getMaxTeamsToVote(), event.getJuryVotingMode());
    }

    @Transactional(readOnly = true)
    // Indica si el usuario ya tiene votos registrados.
    public boolean hasUserVoted(Long userId) {
        return hasUserVoted(userId, null);
    }

    @Transactional(readOnly = true)
    // Indica si el usuario ya voto en el evento seleccionado.
    public boolean hasUserVoted(Long userId, Long eventId) {
        if (userId == null) {
            return false;
        }
        EventEntity event = eventSettingsService.getEventOrActive(eventId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        if (user.getRole() == UserRole.JURY && event.getJuryVotingMode() == JuryVotingMode.MULTICRITERIA) {
            long totalParticipants = participantService.findAll(event.getId()).size();
            if (totalParticipants == 0) {
                return false;
            }
            long evaluatedParticipants = voteRepository.countDistinctParticipantsByEventAndUserIdAndVoteCategory(
                    event,
                    userId,
                    VoteCategory.JURY_MULTICRITERIA
            );
            return evaluatedParticipants >= totalParticipants;
        }
        return voteRepository.existsByEventAndUserId(event, userId);
    }

    @Transactional(readOnly = true)
    // Devuelve los equipos que el jurado actual ya evaluó en modo multicriterio.
    public List<String> getEvaluatedJuryTeamNames(Long userId, Long eventId) {
        if (userId == null) {
            return List.of();
        }
        EventEntity event = eventSettingsService.getEventOrActive(eventId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() != UserRole.JURY || event.getJuryVotingMode() != JuryVotingMode.MULTICRITERIA) {
            return List.of();
        }
        return voteRepository.findDistinctTeamNamesByEventAndUserIdAndVoteCategory(
                event,
                userId,
                VoteCategory.JURY_MULTICRITERIA
        );
    }

    // Normaliza las selecciones eliminando nulos y espacios sobrantes.
    private List<String> normalizeSelections(List<String> selections) {
        List<String> normalizedSelections = new ArrayList<>();
        if (selections == null) {
            return normalizedSelections;
        }
        for (String selection : selections) {
            if (selection == null) {
                continue;
            }
            String trimmed = selection.trim();
            if (!trimmed.isEmpty()) {
                normalizedSelections.add(trimmed);
            }
        }
        return normalizedSelections;
    }

    // Construye la entidad de voto añadiendo criterio y puntuacion cuando aplica.
    private @NonNull VoteEntity buildVoteEntity(
            ParticipantEntity participant,
            EventEntity event,
            Long userId,
            UserRole voterRole,
            VoteCategory voteCategory,
            String criterionKey,
            Integer scoreValue,
            String commentText
    ) {
        VoteEntity entity = new VoteEntity();
        entity.setParticipant(participant);
        entity.setEvent(event);
        entity.setUserId(userId);
        entity.setVoterRole(voterRole);
        entity.setVoteCategory(voteCategory);
        entity.setCriterionKey(criterionKey);
        entity.setScoreValue(scoreValue);
        entity.setCommentText(trimToNull(commentText));
        return entity;
    }

    // Convierte proyecciones del repositorio en DTOs de salida.
    private List<ResultItemResponse> toResultItems(List<VoteTallyProjection> tally) {
        return tally.stream()
                .map(item -> new ResultItemResponse(item.getTeamName(), item.getVotes()))
                .toList();
    }

    // Evita valores nulos en colecciones inmutables.
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    // Normaliza la clave de un criterio del jurado para validarla y persistirla.
    private String normalizeCriterion(String criterion) {
        return criterion == null
                ? ""
                : criterion.trim().toLowerCase(Locale.ROOT).replace("ó", "o");
    }

    // Normaliza las selecciones públicas permitiendo comentarios por equipo.
    private List<VoteSelectionRequest> normalizeSelectionEntries(VoteRequest request) {
        if (request.selectionEntries() != null && !request.selectionEntries().isEmpty()) {
            List<VoteSelectionRequest> normalizedEntries = new ArrayList<>();
            for (VoteSelectionRequest entry : request.selectionEntries()) {
                if (entry == null || entry.teamName() == null) {
                    continue;
                }
                String trimmedTeamName = entry.teamName().trim();
                if (!trimmedTeamName.isEmpty()) {
                    normalizedEntries.add(new VoteSelectionRequest(trimmedTeamName, trimToNull(entry.comment())));
                }
            }
            return normalizedEntries;
        }
        return normalizeSelections(request.selections()).stream()
                .map(selection -> new VoteSelectionRequest(selection, null))
                .toList();
    }

    // Limpia un texto y devuelve null si no queda contenido.
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Construye el bloque privado del equipo del usuario en resultados.
    private MyTeamResultsResponse buildMyTeamResults(EventEntity event, Long userId, List<ResultItemResponse> publicResults) {
        if (userId == null) {
            return null;
        }
        return participantService.findMine(userId, event.getId())
                .map(participant -> {
                    String teamName = participant.teamName();
                    long votes = 0L;
                    int position = 0;
                    for (int i = 0; i < publicResults.size(); i++) {
                        ResultItemResponse item = publicResults.get(i);
                        if (item.teamName() != null && item.teamName().equalsIgnoreCase(teamName)) {
                            votes = item.votes();
                            position = i + 1;
                            break;
                        }
                    }
                    ParticipantEntity entity = participantService.getByTeamName(teamName, event.getId());
                    List<TeamCommentProjection> comments = voteRepository.findCommentsByEventAndParticipantId(event, entity.getId());
                    return new MyTeamResultsResponse(
                            teamName,
                            votes,
                            position,
                            comments.size(),
                            comments.stream().map(this::toCommentResponse).toList()
                    );
                })
                .orElse(null);
    }

    // Convierte la proyección del repositorio en un comentario de salida.
    private TeamCommentResponse toCommentResponse(TeamCommentProjection projection) {
        String role = projection.getVoterRole() == null ? "" : projection.getVoterRole().trim().toUpperCase(Locale.ROOT);
        String authorLabel = "JURY".equals(role) ? "Miembro del jurado" : "Votante anónimo";
        return new TeamCommentResponse(authorLabel, projection.getComment(), projection.getCreatedAt());
    }
}
