package com.votify.backend.service;

import com.votify.backend.domain.vote.Vote;
import com.votify.backend.dto.ResultItemResponse;
import com.votify.backend.dto.ResultsResponse;
import com.votify.backend.dto.VoteRequest;
import com.votify.backend.dto.VoteResponse;
import com.votify.backend.dto.VoteSettingsResponse;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.VoteEntity;
import com.votify.backend.exception.ApiException;
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
    private final EventSettingsService eventSettingsService;
    private final UserRepository userRepository;

    // Inyecta repositorios y servicios necesarios para registrar votos.
    public VoteService(
            VoteJpaRepository voteRepository,
            ParticipantService participantService,
            PublicVoteCreator voteCreator,
            EventSettingsService eventSettingsService,
            UserRepository userRepository
    ) {
        this.voteRepository = voteRepository;
        this.participantService = participantService;
        this.voteCreator = voteCreator;
        this.eventSettingsService = eventSettingsService;
        this.userRepository = userRepository;
    }

    @Transactional
    // Registra los votos de un usuario validando límites, duplicados y equipos existentes.
    public VoteResponse createVotes(VoteRequest request, Long userId) {
        if (!eventSettingsService.isVotingOpen()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Las votaciones estan cerradas actualmente");
        }
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no registrado");
        }
        // Comprobamos si el usuario ya ha votado. Necesitarás añadir `existsByUserId` a tu VoteJpaRepository.
        if (voteRepository.existsByUserId(userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya has votado. No puedes votar de nuevo.");
        }

        List<String> normalizedSelections = normalizeSelections(request.selections());
        int maxTeamsToVote = eventSettingsService.getMaxTeamsToVote();
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
            if (!participantService.existsByTeamName(teamName)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El equipo no existe: " + teamName);
            }
        }

        for (String teamName : orderedSelections) {
            Vote vote = voteCreator.orderVote(teamName);
            ParticipantEntity participant = participantService.getByTeamName(vote.getOption());
            VoteEntity entity = new VoteEntity();
            entity.setParticipant(participant);
            entity.setUserId(userId);
            voteRepository.save(entity);
        }

        return new VoteResponse(orderedSelections.size(), orderedSelections);
    }

    @Transactional(readOnly = true)
    // Calcula y devuelve el resumen agregado de resultados.
    public ResultsResponse getResults() {
        if (!eventSettingsService.areResultsVisible()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Los resultados estan ocultos actualmente por el administrador");
        }
        List<VoteTallyProjection> tally = voteRepository.tally();
        List<ResultItemResponse> results = tally.stream()
                .map(item -> new ResultItemResponse(item.getTeamName(), item.getVotes()))
                .toList();
        return new ResultsResponse(voteRepository.count(), results);
    }

    @Transactional(readOnly = true)
    // Devuelve la configuración de votación consumida por el frontend.
    public VoteSettingsResponse getVoteSettings() {
        return new VoteSettingsResponse(eventSettingsService.getMaxTeamsToVote());
    }

    @Transactional(readOnly = true)
    // Indica si el usuario ya tiene votos registrados.
    public boolean hasUserVoted(Long userId) {
        if (userId == null) {
            return false;
        }
        // Necesitarás añadir `boolean existsByUserId(Long userId);` a tu interface VoteJpaRepository.
        return voteRepository.existsByUserId(userId);
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
}
