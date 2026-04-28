package com.votify.backend.service;

import com.votify.backend.domain.event.ClosedEventState;
import com.votify.backend.domain.event.EventSettingsStateContext;
import com.votify.backend.domain.event.RegistrationOpenState;
import com.votify.backend.domain.event.VotingOpenState;
import com.votify.backend.dto.EventSettingsDto;
import com.votify.backend.entity.EventSettingsEntity;
import com.votify.backend.exception.ApiException;
import com.votify.backend.repository.EventSettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventSettingsService {
    private static final int DEFAULT_MAX_TEAMS_TO_VOTE = 1;

    private final EventSettingsRepository eventSettingsRepository;

    public EventSettingsService(EventSettingsRepository eventSettingsRepository) {
        this.eventSettingsRepository = eventSettingsRepository;
    }

    @Transactional(readOnly = true)
    public EventSettingsDto getSettings() {
        EventSettingsEntity entity = getOrCreateSettings();
        return EventSettingsStateContext.fromEntity(entity).toDto();
    }

    @Transactional
    public void updateSettings(EventSettingsDto settings) {
        if (settings.maxTeamsToVote() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El número de votos debe ser al menos 1");
        }

        if (settings.registrationsOpen()) {
            openRegistrations(settings.resultsVisible(), settings.maxTeamsToVote());
            return;
        }
        if (settings.votingOpen()) {
            openVoting(settings.resultsVisible(), settings.maxTeamsToVote());
            return;
        }
        closeEvent(settings.resultsVisible(), settings.maxTeamsToVote());
    }

    @Transactional
    public void openRegistrations(boolean resultsVisible, int maxTeamsToVote) {
        updatePhase(new RegistrationOpenState(), resultsVisible, maxTeamsToVote);
    }

    @Transactional
    public void openVoting(boolean resultsVisible, int maxTeamsToVote) {
        updatePhase(new VotingOpenState(), resultsVisible, maxTeamsToVote);
    }

    @Transactional
    public void closeEvent(boolean resultsVisible, int maxTeamsToVote) {
        updatePhase(new ClosedEventState(), resultsVisible, maxTeamsToVote);
    }

    @Transactional(readOnly = true)
    public boolean areRegistrationsOpen() {
        return currentContext().registrationsOpen();
    }

    @Transactional(readOnly = true)
    public boolean isVotingOpen() {
        return currentContext().votingOpen();
    }

    @Transactional(readOnly = true)
    public boolean areResultsVisible() {
        return currentContext().resultsVisible();
    }

    @Transactional(readOnly = true)
    public int getMaxTeamsToVote() {
        return currentContext().maxTeamsToVote();
    }

    @Transactional(readOnly = true)
    public boolean allowsTeamRegistration() {
        return currentContext().registrationsOpen();
    }

    @Transactional(readOnly = true)
    public boolean allowsVoting() {
        return currentContext().votingOpen();
    }

    @Transactional(readOnly = true)
    public boolean allowsResultsVisibility() {
        return currentContext().resultsVisible();
    }

    private EventSettingsEntity getOrCreateSettings() {
        return eventSettingsRepository.findById(EventSettingsEntity.SINGLETON_ID)
                .orElseGet(this::createDefaultSettings);
    }

    private EventSettingsEntity createDefaultSettings() {
        EventSettingsEntity entity = new EventSettingsEntity();
        entity.setId(EventSettingsEntity.SINGLETON_ID);
        EventSettingsStateContext context = EventSettingsStateContext.fromEntity(defaultSettings(entity));
        context.applyTo(entity);
        return eventSettingsRepository.save(entity);
    }

    private EventSettingsStateContext currentContext() {
        return EventSettingsStateContext.fromEntity(getOrCreateSettings());
    }

    private EventSettingsEntity defaultSettings(EventSettingsEntity entity) {
        entity.setRegistrationsOpen(true);
        entity.setVotingOpen(false);
        entity.setResultsVisible(false);
        entity.setMaxTeamsToVote(DEFAULT_MAX_TEAMS_TO_VOTE);
        entity.setPhase("REGISTRATION_OPEN");
        return entity;
    }

    private void updatePhase(
            com.votify.backend.domain.event.EventPhaseState phaseState,
            boolean resultsVisible,
            int maxTeamsToVote
    ) {
        if (maxTeamsToVote < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El número de votos debe ser al menos 1");
        }

        EventSettingsEntity entity = getOrCreateSettings();
        EventSettingsStateContext context = EventSettingsStateContext.fromEntity(entity);
        EventSettingsDto transition = new EventSettingsDto(
                phaseState.registrationsOpen(),
                phaseState.votingOpen(),
                resultsVisible,
                maxTeamsToVote
        );
        context.applyAdminSelection(transition);
        context.applyTo(entity);
        java.util.Objects.requireNonNull(entity);
        eventSettingsRepository.save(entity);
    }
}
