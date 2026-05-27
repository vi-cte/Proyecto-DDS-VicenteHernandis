package com.votify.backend.service;

import com.votify.backend.dto.AdminEventRequest;
import com.votify.backend.dto.AdminEventResponse;
import com.votify.backend.dto.ResultItemResponse;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.EventPhase;
import com.votify.backend.entity.JuryVotingMode;
import com.votify.backend.entity.UserRole;
import com.votify.backend.exception.ApiException;
import com.votify.backend.observer.VoteEventPublisher;
import com.votify.backend.repository.EventJpaRepository;
import com.votify.backend.repository.ParticipantJpaRepository;
import com.votify.backend.repository.VoteJpaRepository;
import com.votify.backend.repository.VoteTallyProjection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
// Casos de uso administrativos para gestionar eventos.
public class EventAdminService {
    private final EventJpaRepository eventRepository;
    private final ParticipantJpaRepository participantRepository;
    private final VoteJpaRepository voteRepository;
    private final JdbcTemplate jdbcTemplate;
    private final VoteEventPublisher voteEventPublisher;

    public EventAdminService(EventJpaRepository eventRepository, ParticipantJpaRepository participantRepository, VoteJpaRepository voteRepository, JdbcTemplate jdbcTemplate, VoteEventPublisher voteEventPublisher) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.voteRepository = voteRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.voteEventPublisher = voteEventPublisher;
    }

    @Transactional(readOnly = true)
    // Devuelve todos los eventos con métricas de dashboard.
    public List<AdminEventResponse> findAll() {
        return eventRepository.findAllByOrderByActiveDescEventDateDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    // Crea un nuevo evento disponible sin desactivar eventos anteriores.
    public AdminEventResponse create(AdminEventRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre del evento es obligatorio");
        }
        if (request.maxTeamsToVote() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El número de votos debe ser al menos 1");
        }

        EventEntity event = new EventEntity();
        event.setName(request.name().trim());
        event.setEventDate(request.eventDate());
        event.setDescription(trimToNull(request.description()));
        event.setMaxTeamsToVote(request.maxTeamsToVote());
        event.setJuryEnabled(request.juryEnabled());
        event.setJuryVotingMode(request.juryVotingMode() == null ? JuryVotingMode.SIMPLE : request.juryVotingMode());
        event.setPhase(resolvePhase(request));
        EventEntity saved = eventRepository.save(event);
        voteEventPublisher.notifyVotesChangedAfterCommit(saved.getId());
        return toResponse(saved);
    }

    @Transactional
    // Actualiza los ajustes de un evento existente desde administración.
    public AdminEventResponse update(Long id, AdminEventRequest request) {
        if (id == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El ID del evento es obligatorio");
        }
        if (request.maxTeamsToVote() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El número de votos debe ser al menos 1");
        }

        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El evento no existe"));
        if (request.name() != null && !request.name().isBlank()) {
            event.setName(request.name().trim());
        }
        JuryVotingMode requestedMode = request.juryVotingMode() == null ? JuryVotingMode.SIMPLE : request.juryVotingMode();
        if (event.getJuryVotingMode() != requestedMode
                && voteRepository.existsByEventAndVoterRole(event, UserRole.JURY)) {
            throw new ApiException(HttpStatus.CONFLICT, "No puedes cambiar el modo del jurado cuando ya hay votos del jurado registrados");
        }
        event.setEventDate(request.eventDate());
        event.setDescription(trimToNull(request.description()));
        event.setMaxTeamsToVote(request.maxTeamsToVote());
        event.setJuryEnabled(request.juryEnabled());
        event.setJuryVotingMode(requestedMode);
        event.setPhase(resolvePhase(request));
        EventEntity saved = eventRepository.save(event);
        voteEventPublisher.notifyVotesChangedAfterCommit(saved.getId());
        return toResponse(saved);
    }

    @Transactional
    // Archiva un evento aplicando el estado de dominio correspondiente.
    public void archive(@NonNull Long id) {
        EventEntity event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El evento no existe"));
        event.setPhase(EventPhase.ARCHIVED);
        eventRepository.save(event);
        voteEventPublisher.notifyVotesChangedAfterCommit(event.getId());
    }

    @Transactional
    // Elimina un evento y todos sus datos asociados.
    public void delete(@NonNull Long id) {
        EventEntity event = Objects.requireNonNull(eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El evento no existe")));
        jdbcTemplate.update("DELETE FROM votes WHERE event_id = ?", id);
        jdbcTemplate.update("DELETE FROM participant_members WHERE participant_id IN (SELECT id FROM participants WHERE event_id = ?)", id);
        jdbcTemplate.update("DELETE FROM participants WHERE event_id = ?", id);
        eventRepository.delete(event);
        voteEventPublisher.notifyVotesChangedAfterCommit(id);
    }

    // Convierte entidad en DTO administrativo con ranking.
    private AdminEventResponse toResponse(@NonNull EventEntity event) {
        List<ResultItemResponse> ranking = withRegisteredParticipants(event, voteRepository.tallyByEvent(event));
        List<ResultItemResponse> publicRanking = withRegisteredParticipants(event, voteRepository.tallyByEventAndVoterRole(event, UserRole.PUBLIC));
        List<ResultItemResponse> juryRanking = withRegisteredParticipants(event, voteRepository.tallyByEventAndVoterRole(event, UserRole.JURY));
        long totalPublicVotes = voteRepository.sumScoreByEventAndVoterRole(event, UserRole.PUBLIC);
        long totalJuryVotes = voteRepository.sumScoreByEventAndVoterRole(event, UserRole.JURY);
        
        return new AdminEventResponse(
                event.getId(),
                event.getName(),
                event.getEventDate(),
                event.getDescription(),
                event.isRegistrationsOpen(),
                event.isVotingOpen(),
                event.isResultsVisible(),
                event.getMaxTeamsToVote(),
                event.isJuryEnabled(),
                event.getJuryVotingMode(),
                event.getPhase(),
                event.isActive(),
                totalPublicVotes + totalJuryVotes,
                participantRepository.countByEvent(event),
                ranking,
                publicRanking,
                juryRanking
        );
    }

    private List<ResultItemResponse> withRegisteredParticipants(EventEntity event, List<VoteTallyProjection> tally) {
        List<ResultItemResponse> ranking = new java.util.ArrayList<>(tally.stream()
                .map(this::toResult)
                .toList());
        List<com.votify.backend.entity.ParticipantEntity> participants = participantRepository.findAllByEvent(event);
        for (com.votify.backend.entity.ParticipantEntity p : participants) {
            if (ranking.stream().noneMatch(r -> r.teamName().equals(p.getTeamName()))) {
                ranking.add(new ResultItemResponse(p.getTeamName(), 0L));
            }
        }
        return ranking;
    }

    private ResultItemResponse toResult(VoteTallyProjection projection) {
        return new ResultItemResponse(projection.getTeamName(), projection.getVotes());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private EventPhase resolvePhase(AdminEventRequest request) {
        if (request.phase() != null) {
            return request.phase();
        }
        if (request.resultsVisible()) {
            return EventPhase.RESULTS_VISIBLE;
        }
        if (request.votingOpen() && request.juryEnabled()) {
            return EventPhase.PUBLIC_AND_JURY_VOTING_OPEN;
        }
        if (request.votingOpen()) {
            return EventPhase.PUBLIC_VOTING_OPEN;
        }
        if (request.registrationsOpen()) {
            return EventPhase.REGISTRATION_OPEN;
        }
        return EventPhase.REGISTRATION_CLOSED;
    }
}
