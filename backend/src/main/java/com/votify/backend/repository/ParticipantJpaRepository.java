package com.votify.backend.repository;

import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repositorio JPA para acceder a participantes.
public interface ParticipantJpaRepository extends JpaRepository<ParticipantEntity, Long> {
    // Indica si existe un equipo con ese nombre, ignorando mayusculas/minusculas.
    boolean existsByTeamNameIgnoreCase(String teamName);
    boolean existsByEventAndTeamNameIgnoreCase(EventEntity event, String teamName);

    // Busca un equipo por nombre ignorando mayúsculas y minúsculas.
    Optional<ParticipantEntity> findByTeamNameIgnoreCase(String teamName);
    Optional<ParticipantEntity> findByEventAndTeamNameIgnoreCase(EventEntity event, String teamName);

    // Busca el equipo asociado al correo del usuario propietario.
    Optional<ParticipantEntity> findByOwnerEmailIgnoreCase(String ownerEmail);
    Optional<ParticipantEntity> findByEventAndOwnerEmailIgnoreCase(EventEntity event, String ownerEmail);
    List<ParticipantEntity> findAllByEvent(EventEntity event);
    List<ParticipantEntity> findAllByEventIsNull();
    long countByEvent(EventEntity event);
}
