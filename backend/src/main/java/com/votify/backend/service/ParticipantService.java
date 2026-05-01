package com.votify.backend.service;

import com.votify.backend.builder.DefaultParticipantBuilder;
import com.votify.backend.builder.ParticipantDirector;
import com.votify.backend.dto.ParticipantRequest;
import com.votify.backend.dto.ParticipantResponse;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.User;
import com.votify.backend.exception.ApiException;
import com.votify.backend.repository.ParticipantJpaRepository;
import com.votify.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
// Gestiona las reglas de negocio de equipos participantes.
public class ParticipantService {
    private final ParticipantJpaRepository participantRepository;
    private final UserRepository userRepository;
    private final EventSettingsService eventSettingsService;

    // Inyecta los repositorios de participantes y usuarios.
    public ParticipantService(ParticipantJpaRepository participantRepository, UserRepository userRepository, EventSettingsService eventSettingsService) {
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.eventSettingsService = eventSettingsService;
    }

    @Transactional
    // Crea un equipo para el usuario autenticado validando duplicados.
    public ParticipantResponse create(ParticipantRequest request, Long userId) {
        if (!eventSettingsService.areRegistrationsOpen()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El registro de equipos se encuentra cerrado");
        }

        User currentUser = getUser(userId);
        String normalizedTeamName = request.teamName().trim();
        if (participantRepository.existsByTeamNameIgnoreCase(normalizedTeamName)) {
            throw new ApiException(HttpStatus.CONFLICT, "El nombre del equipo ya esta registrado");
        }
        if (participantRepository.findByOwnerEmailIgnoreCase(currentUser.getEmail()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Este usuario ya tiene un equipo registrado");
        }

        ParticipantDirector director = new ParticipantDirector(new DefaultParticipantBuilder());
        ParticipantEntity entity = director.buildParticipant(
                normalizedTeamName,
                request.email().trim(),
                trimToNull(request.phone()),
                trimToNull(request.description()),
                trimToNull(request.logo()),
                request.members(),
                currentUser.getEmail()
        );

        ParticipantEntity saved = participantRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    // Actualiza un equipo existente si pertenece al usuario autenticado.
    public ParticipantResponse update(Long id, ParticipantRequest request, Long userId) {
        User currentUser = getUser(userId);
        if (id == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El ID del participante no puede ser nulo");
        }

        ParticipantEntity entity = participantRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El participante no existe"));
        if (entity.getOwnerEmail() == null || !entity.getOwnerEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes editar un equipo que no te pertenece");
        }

        String normalizedTeamName = request.teamName().trim();
        if (!entity.getTeamName().equalsIgnoreCase(normalizedTeamName) &&
                participantRepository.existsByTeamNameIgnoreCase(normalizedTeamName)) {
            throw new ApiException(HttpStatus.CONFLICT, "El nombre del equipo ya esta registrado");
        }

        entity.setTeamName(normalizedTeamName);
        entity.setEmail(request.email().trim());
        entity.setPhone(trimToNull(request.phone()));
        entity.setDescription(trimToNull(request.description()));
        entity.setLogo(trimToNull(request.logo()));
        entity.setMembers(request.members());
        entity.setOwnerEmail(currentUser.getEmail());

        ParticipantEntity saved = participantRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    // Devuelve todos los equipos participantes como DTOs.
    public List<ParticipantResponse> findAll() {
        return participantRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    // Busca el equipo registrado por el usuario autenticado.
    public Optional<ParticipantResponse> findMine(Long userId) {
        User currentUser = getUser(userId);
        return participantRepository.findByOwnerEmailIgnoreCase(currentUser.getEmail())
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    // Comprueba si existe un equipo con el nombre indicado.
    public boolean existsByTeamName(String teamName) {
        return participantRepository.existsByTeamNameIgnoreCase(teamName);
    }

    @Transactional(readOnly = true)
    // Devuelve la entidad de un equipo o lanza error si no existe.
    public ParticipantEntity getByTeamName(String teamName) {
        return participantRepository.findByTeamNameIgnoreCase(teamName)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El equipo no existe: " + teamName));
    }

    // Convierte una entidad participante en su DTO de respuesta.
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

    // Limpia un texto y devuelve null si queda vacío.
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Obtiene el usuario autenticado o lanza error de sesión.
    private User getUser(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para realizar esta acción");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "La sesión de usuario no es válida"));
    }
}
