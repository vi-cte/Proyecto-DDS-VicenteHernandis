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
public class VoteService {
    private static final int MAX_TEAMS_TO_VOTE = 3;

    private final VoteJpaRepository voteRepository;
    private final ParticipantService participantService;
    private final PublicVoteCreator voteCreator;

    public VoteService(
            VoteJpaRepository voteRepository,
            ParticipantService participantService,
            PublicVoteCreator voteCreator
    ) {
        this.voteRepository = voteRepository;
        this.participantService = participantService;
        this.voteCreator = voteCreator;
    }

    @Transactional
    public VoteResponse createVotes(VoteRequest request, Long userId) {
        // Comprobamos si el usuario ya ha votado. Necesitarás añadir `existsByUserId` a tu VoteJpaRepository.
        if (voteRepository.existsByUserId(userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya has votado. No puedes votar de nuevo.");
        }

        List<String> normalizedSelections = normalizeSelections(request.selections());
        if (normalizedSelections.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes seleccionar al menos un participante");
        }
        if (normalizedSelections.size() > MAX_TEAMS_TO_VOTE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Solo puedes votar a " + MAX_TEAMS_TO_VOTE + " equipos");
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
    public ResultsResponse getResults() {
        List<VoteTallyProjection> tally = voteRepository.tally();
        List<ResultItemResponse> results = tally.stream()
                .map(item -> new ResultItemResponse(item.getTeamName(), item.getVotes()))
                .toList();
        return new ResultsResponse(voteRepository.count(), results);
    }

    @Transactional(readOnly = true)
    public VoteSettingsResponse getVoteSettings() {
        return new VoteSettingsResponse(MAX_TEAMS_TO_VOTE);
    }

    @Transactional(readOnly = true)
    public boolean hasUserVoted(Long userId) {
        // Necesitarás añadir `boolean existsByUserId(Long userId);` a tu interface VoteJpaRepository.
        return voteRepository.existsByUserId(userId);
    }

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
