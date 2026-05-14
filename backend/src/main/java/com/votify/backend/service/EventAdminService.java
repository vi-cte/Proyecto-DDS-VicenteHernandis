package com.votify.backend.service;

import com.votify.backend.dto.AdminEventRequest;
import com.votify.backend.dto.AdminEventResponse;
import com.votify.backend.dto.ResultItemResponse;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.JuryVotingMode;
import com.votify.backend.entity.UserRole;
import com.votify.backend.exception.ApiException;
import com.votify.backend.repository.EventJpaRepository;
import com.votify.backend.repository.ParticipantJpaRepository;
import com.votify.backend.repository.VoteJpaRepository;
import com.votify.backend.repository.VoteTallyProjection;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
// Casos de uso administrativos para gestionar eventos.
public class EventAdminService {
    private final EventJpaRepository eventRepository;
    private final ParticipantJpaRepository participantRepository;
    private final VoteJpaRepository voteRepository;

    public EventAdminService(EventJpaRepository eventRepository, ParticipantJpaRepository participantRepository, VoteJpaRepository voteRepository) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.voteRepository = voteRepository;
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
        event.setRegistrationsOpen(request.registrationsOpen());
        event.setVotingOpen(request.votingOpen() && !request.registrationsOpen());
        event.setResultsVisible(request.resultsVisible());
        event.setMaxTeamsToVote(request.maxTeamsToVote());
        event.setJuryEnabled(request.juryEnabled());
        event.setJuryVotingMode(request.juryVotingMode() == null ? JuryVotingMode.SIMPLE : request.juryVotingMode());
        event.setActive(true);
        return toResponse(eventRepository.save(event));
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
        event.setRegistrationsOpen(request.registrationsOpen());
        event.setVotingOpen(request.votingOpen() && !request.registrationsOpen());
        event.setResultsVisible(request.resultsVisible());
        event.setMaxTeamsToVote(request.maxTeamsToVote());
        event.setJuryEnabled(request.juryEnabled());
        event.setJuryVotingMode(requestedMode);
        return toResponse(eventRepository.save(event));
    }

    // Convierte entidad en DTO administrativo con ranking.
    private AdminEventResponse toResponse(EventEntity event) {
        List<ResultItemResponse> ranking = voteRepository.tallyByEvent(event).stream()
                .map(this::toResult)
                .toList();
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
                event.isActive(),
                voteRepository.countByEvent(event),
                participantRepository.countByEvent(event),
                ranking
        );
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
}
