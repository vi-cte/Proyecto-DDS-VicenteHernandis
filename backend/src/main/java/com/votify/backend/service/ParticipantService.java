package com.votify.backend.service;

import com.votify.backend.dto.ParticipantRequest;
import com.votify.backend.dto.ParticipantResponse;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.exception.ApiException;
import com.votify.backend.repository.ParticipantJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParticipantService {
    private final ParticipantJpaRepository participantRepository;

    public ParticipantService(ParticipantJpaRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    @Transactional
    public ParticipantResponse create(ParticipantRequest request) {
        String normalizedTeamName = request.teamName().trim();
        if (participantRepository.existsByTeamNameIgnoreCase(normalizedTeamName)) {
            throw new ApiException(HttpStatus.CONFLICT, "El nombre del equipo ya esta registrado");
        }

        ParticipantEntity entity = new ParticipantEntity();
        entity.setTeamName(normalizedTeamName);
        entity.setEmail(request.email().trim());
        entity.setPhone(trimToNull(request.phone()));
        entity.setDescription(trimToNull(request.description()));
        entity.setLogo(trimToNull(request.logo()));
        entity.setMembers(request.members());

        ParticipantEntity saved = participantRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> findAll() {
        return participantRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByTeamName(String teamName) {
        return participantRepository.existsByTeamNameIgnoreCase(teamName);
    }

    @Transactional(readOnly = true)
    public ParticipantEntity getByTeamName(String teamName) {
        return participantRepository.findByTeamNameIgnoreCase(teamName)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El equipo no existe: " + teamName));
    }

    private ParticipantResponse toResponse(ParticipantEntity entity) {
        return new ParticipantResponse(
                entity.getId(),
                entity.getTeamName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getDescription(),
                entity.getLogo(),
                entity.getMembers()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
