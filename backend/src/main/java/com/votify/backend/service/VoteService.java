package com.votify.backend.service;

import com.votify.backend.domain.vote.Vote;
import com.votify.backend.dto.ResultItemResponse;
import com.votify.backend.dto.ResultsResponse;
import com.votify.backend.dto.VoteRequest;
import com.votify.backend.dto.VoteResponse;
import com.votify.backend.dto.VoteSettingsResponse;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.User;
import com.votify.backend.entity.UserRole;
import com.votify.backend.entity.VoteCategory;
import com.votify.backend.entity.VoteEntity;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.exception.ApiException;
import com.votify.backend.factory.JuryVoteCreator;
import com.votify.backend.factory.PublicVoteCreator;
import com.votify.backend.repository.UserRepository;
import com.votify.backend.repository.VoteJpaRepository;
import com.votify.backend.repository.VoteTallyProjection;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
// Gestiona registro de votos, consulta de resultados y límites de votación.
public class VoteService {
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
        // Comprobamos si el usuario ya ha votado.
        if (voteRepository.existsByEventAndUserId(event, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya has votado. No puedes votar de nuevo.");
        }

        if (user.getRole() == UserRole.JURY) {
            return createJuryVotes(request, userId, event);
        }
        return createPublicVotes(request, userId, event);
    }

    // Registra votos públicos con el límite configurado por el administrador.
    private VoteResponse createPublicVotes(VoteRequest request, Long userId, EventEntity activeEvent) {
        List<String> normalizedSelections = normalizeSelections(request.selections());
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
            voteRepository.save(buildVoteEntity(participant, activeEvent, userId, UserRole.PUBLIC, VoteCategory.PUBLIC_WINNER));
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
        voteRepository.save(buildVoteEntity(winner, activeEvent, userId, UserRole.JURY, VoteCategory.JURY_WINNER));

        Vote technicalVote = juryVoteCreator.orderVote(jurySelections.get(1));
        ParticipantEntity technical = participantService.getByTeamName(technicalVote.getOption(), activeEvent.getId());
        voteRepository.save(buildVoteEntity(technical, activeEvent, userId, UserRole.JURY, VoteCategory.JURY_TECHNICAL));

        return new VoteResponse(jurySelections.size(), jurySelections);
    }

    @Transactional(readOnly = true)
    // Calcula y devuelve el resumen agregado de resultados.
    public ResultsResponse getResults() {
        return getResults(null);
    }

    @Transactional(readOnly = true)
    // Calcula y devuelve el resumen agregado de resultados del evento seleccionado.
    public ResultsResponse getResults(Long eventId) {
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
                juryResults
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
        return new VoteSettingsResponse(eventSettingsService.getEventOrActive(eventId).getMaxTeamsToVote());
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
        return voteRepository.existsByEventAndUserId(eventSettingsService.getEventOrActive(eventId), userId);
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

    // Construye la entidad de voto común para público y jurado.
    private VoteEntity buildVoteEntity(ParticipantEntity participant, EventEntity event, Long userId, UserRole voterRole, VoteCategory voteCategory) {
        VoteEntity entity = new VoteEntity();
        entity.setParticipant(participant);
        entity.setEvent(event);
        entity.setUserId(userId);
        entity.setVoterRole(voterRole);
        entity.setVoteCategory(voteCategory);
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
}
