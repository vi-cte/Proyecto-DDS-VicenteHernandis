package com.votify.backend.service;

import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.entity.EventSettingsEntity;
import com.votify.backend.exception.ApiException;
import com.votify.backend.repository.EventSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// Gestiona la configuración global del evento.
public class EventSettingsService {
    private final EventSettingsRepository eventSettingsRepository;

    // Inyecta el repositorio de configuración del evento.
    public EventSettingsService(EventSettingsRepository eventSettingsRepository) {
        this.eventSettingsRepository = eventSettingsRepository;
    }

    @Transactional(readOnly = true)
    // Devuelve la configuración actual creando valores por defecto si faltan.
    public EventSettingsDto getSettings() {
        EventSettingsEntity entity = getOrCreateSettings();
        return new EventSettingsDto(
                entity.isRegistrationsOpen(),
                entity.isVotingOpen(),
                entity.isResultsVisible(),
                entity.getMaxTeamsToVote()
        );
    }

    @Transactional
    // Actualiza los flags del evento y valida el número mínimo de votos.
    public void updateSettings(EventSettingsDto settings) {
        if (settings.maxTeamsToVote() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El número de votos debe ser al menos 1");
        }

        EventSettingsEntity entity = getOrCreateSettings();
        entity.setRegistrationsOpen(settings.registrationsOpen());
        entity.setVotingOpen(settings.votingOpen() && !settings.registrationsOpen());
        entity.setResultsVisible(settings.resultsVisible());
        entity.setMaxTeamsToVote(settings.maxTeamsToVote());
        eventSettingsRepository.save(entity);
    }

    @Transactional(readOnly = true)
    // Indica si el registro de equipos está abierto.
    public boolean areRegistrationsOpen() {
        return getOrCreateSettings().isRegistrationsOpen();
    }

    @Transactional(readOnly = true)
    // Indica si la votación está abierta.
    public boolean isVotingOpen() {
        return getOrCreateSettings().isVotingOpen();
    }

    @Transactional(readOnly = true)
    // Indica si los resultados pueden mostrarse.
    public boolean areResultsVisible() {
        return getOrCreateSettings().isResultsVisible();
    }

    @Transactional(readOnly = true)
    // Devuelve el número máximo de equipos que puede votar un usuario.
    public int getMaxTeamsToVote() {
        return getOrCreateSettings().getMaxTeamsToVote();
    }

    @Transactional(readOnly = true)
    // Alias de lectura usado por las reglas de acceso a inscripción.
    public boolean allowsTeamRegistration() {
        return areRegistrationsOpen();
    }

    @Transactional(readOnly = true)
    // Alias de lectura usado por las reglas de acceso a votación.
    public boolean allowsVoting() {
        return isVotingOpen();
    }

    @Transactional(readOnly = true)
    // Alias de lectura usado por las reglas de visibilidad de resultados.
    public boolean allowsResultsVisibility() {
        return areResultsVisible();
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
}
