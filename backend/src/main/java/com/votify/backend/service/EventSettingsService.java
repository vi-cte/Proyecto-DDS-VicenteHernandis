package com.votify.backend.service;

import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.entity.EventSettingsEntity;
import com.votify.backend.exception.ApiException;
import com.votify.backend.repository.EventSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventSettingsService {
    private final EventSettingsRepository eventSettingsRepository;

    public EventSettingsService(EventSettingsRepository eventSettingsRepository) {
        this.eventSettingsRepository = eventSettingsRepository;
    }

    @Transactional(readOnly = true)
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
    public boolean areRegistrationsOpen() {
        return getOrCreateSettings().isRegistrationsOpen();
    }

    @Transactional(readOnly = true)
    public boolean isVotingOpen() {
        return getOrCreateSettings().isVotingOpen();
    }

    @Transactional(readOnly = true)
    public boolean areResultsVisible() {
        return getOrCreateSettings().isResultsVisible();
    }

    @Transactional(readOnly = true)
    public int getMaxTeamsToVote() {
        return getOrCreateSettings().getMaxTeamsToVote();
    }

    private EventSettingsEntity getOrCreateSettings() {
        return eventSettingsRepository.findById(EventSettingsEntity.SINGLETON_ID)
                .orElseGet(this::createDefaultSettings);
    }

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
