package com.votify.backend.service;

import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.EventSettingsEntity;
import com.votify.backend.entity.JuryVotingMode;
import com.votify.backend.entity.UserRole;
import com.votify.backend.exception.ApiException;
import com.votify.backend.repository.EventJpaRepository;
import com.votify.backend.repository.EventSettingsRepository;
import com.votify.backend.repository.ParticipantJpaRepository;
import com.votify.backend.repository.VoteJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
// Gestiona la configuración global del evento.
public class EventSettingsService {
    private final EventSettingsRepository eventSettingsRepository;
    private final EventJpaRepository eventRepository;
    private final ParticipantJpaRepository participantRepository;
    private final VoteJpaRepository voteRepository;

    // Inyecta el repositorio de configuración del evento.
    public EventSettingsService(
            EventSettingsRepository eventSettingsRepository,
            EventJpaRepository eventRepository,
            ParticipantJpaRepository participantRepository,
            VoteJpaRepository voteRepository
    ) {
        this.eventSettingsRepository = eventSettingsRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    // Devuelve la configuración actual creando valores por defecto si faltan.
    public EventSettingsDto getSettings() {
        EventEntity entity = getOrCreateActiveEvent();
        return new EventSettingsDto(
                entity.isRegistrationsOpen(),
                entity.isVotingOpen(),
                entity.isResultsVisible(),
                entity.getMaxTeamsToVote(),
                entity.getJuryVotingMode()
        );
    }

    @Transactional
    // Actualiza los flags del evento y valida el número mínimo de votos.
    public void updateSettings(EventSettingsDto settings) {
        if (settings.maxTeamsToVote() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El número de votos debe ser al menos 1");
        }

        EventEntity entity = getOrCreateActiveEvent();
        JuryVotingMode requestedMode = settings.juryVotingMode() == null ? JuryVotingMode.SIMPLE : settings.juryVotingMode();
        if (entity.getJuryVotingMode() != requestedMode
                && voteRepository.existsByEventAndVoterRole(entity, UserRole.JURY)) {
            throw new ApiException(HttpStatus.CONFLICT, "No puedes cambiar el modo del jurado cuando ya hay votos del jurado registrados");
        }
        entity.setRegistrationsOpen(settings.registrationsOpen());
        entity.setVotingOpen(settings.votingOpen() && !settings.registrationsOpen());
        entity.setResultsVisible(settings.resultsVisible());
        entity.setMaxTeamsToVote(settings.maxTeamsToVote());
        entity.setJuryVotingMode(requestedMode);
        eventRepository.save(entity);
    }

    @Transactional
    // Indica si el registro de equipos está abierto.
    public boolean areRegistrationsOpen() {
        return getOrCreateActiveEvent().isRegistrationsOpen();
    }

    @Transactional
    // Indica si la votación está abierta.
    public boolean isVotingOpen() {
        return getOrCreateActiveEvent().isVotingOpen();
    }

    @Transactional
    // Indica si los resultados pueden mostrarse.
    public boolean areResultsVisible() {
        return getOrCreateActiveEvent().isResultsVisible();
    }

    @Transactional
    // Devuelve el número máximo de equipos que puede votar un usuario.
    public int getMaxTeamsToVote() {
        return getOrCreateActiveEvent().getMaxTeamsToVote();
    }

    @Transactional
    // Alias de lectura usado por las reglas de acceso a inscripción.
    public boolean allowsTeamRegistration() {
        return areRegistrationsOpen();
    }

    @Transactional
    // Alias de lectura usado por las reglas de acceso a votación.
    public boolean allowsVoting() {
        return isVotingOpen();
    }

    @Transactional
    // Alias de lectura usado por las reglas de visibilidad de resultados.
    public boolean allowsResultsVisibility() {
        return areResultsVisible();
    }

    @Transactional
    // Devuelve el evento activo actual.
    public EventEntity getActiveEvent() {
        return getOrCreateActiveEvent();
    }

    @Transactional
    // Devuelve el evento indicado o el activo por defecto si no se especifica.
    public EventEntity getEventOrActive(Long eventId) {
        if (eventId == null) {
            return getOrCreateActiveEvent();
        }
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El evento no existe"));
    }

    @Transactional(readOnly = true)
    // Devuelve los eventos activos disponibles para pantallas publicas.
    public List<EventEntity> getPublicEvents() {
        return eventRepository.findAllByActiveTrueOrderByEventDateDescIdDesc();
    }

    // Busca la configuración singleton o la crea si aún no existe.
    private EventSettingsEntity getOrCreateSettings() {
        return eventSettingsRepository.findById(EventSettingsEntity.SINGLETON_ID)
                .orElseGet(this::createDefaultSettings);
    }

    // Crea la configuración inicial por defecto del evento.
    private EventSettingsEntity createDefaultSettings() {
        EventSettingsEntity entity = new EventSettingsEntity();
        entity.setId(EventSettingsEntity.SINGLETON_ID);
        entity.setRegistrationsOpen(true);
        entity.setVotingOpen(false);
        entity.setResultsVisible(false);
        entity.setMaxTeamsToVote(1);
        return eventSettingsRepository.save(entity);
    }

    // Busca el evento activo o crea uno inicial compatible con el flujo actual.
    private EventEntity getOrCreateActiveEvent() {
        EventEntity event = eventRepository.findFirstByActiveTrueOrderByIdDesc()
                .orElseGet(this::createDefaultEvent);
        migrateLegacyData(event);
        return event;
    }

    // Crea el evento inicial si la instalación aún no tiene eventos.
    private EventEntity createDefaultEvent() {
        EventSettingsEntity legacy = getOrCreateSettings();
        EventEntity event = new EventEntity();
        event.setName("Evento principal");
        event.setDescription("Evento activo de Votify");
        event.setRegistrationsOpen(legacy.isRegistrationsOpen());
        event.setVotingOpen(legacy.isVotingOpen());
        event.setResultsVisible(legacy.isResultsVisible());
        event.setMaxTeamsToVote(Math.max(1, legacy.getMaxTeamsToVote()));
        event.setJuryEnabled(true);
        event.setJuryVotingMode(JuryVotingMode.SIMPLE);
        event.setActive(true);
        return eventRepository.save(event);
    }

    // Asocia datos antiguos sin event_id al evento activo para no perder equipos ni votos.
    private void migrateLegacyData(EventEntity event) {
        participantRepository.findAllByEventIsNull().forEach(participant -> {
            participant.setEvent(event);
            participantRepository.save(participant);
        });
        voteRepository.findAllByEventIsNull().forEach(vote -> {
            if (vote == null) {
                return;
            }
            vote.setEvent(event);
            voteRepository.save(vote);
        });
    }
}
