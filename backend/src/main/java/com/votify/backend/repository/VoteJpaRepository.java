package com.votify.backend.repository;

import com.votify.backend.entity.VoteEntity;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// Repositorio JPA para acceder a votos y obtener el conteo agregado.
public interface VoteJpaRepository extends JpaRepository<VoteEntity, Long> {
    // Devuelve el conteo de votos por nombre de equipo, ordenado por votos desc y nombre asc.
    @Query("""
            select v.participant.teamName as teamName, count(v) as votes
            from VoteEntity v, ParticipantEntity p
            where v.participant.id = p.id
            group by v.participant.teamName
            order by count(v) desc, v.participant.teamName asc
            """)
    List<VoteTallyProjection> tally();

    // Devuelve el conteo de votos por rol de votante.
    @Query("""
            select v.participant.teamName as teamName, count(v) as votes
            from VoteEntity v
            where v.event = :event
            group by v.participant.teamName
            order by count(v) desc, v.participant.teamName asc
            """)
    List<VoteTallyProjection> tallyByEvent(EventEntity event);

    @Query("""
            select v.participant.teamName as teamName, count(v) as votes
            from VoteEntity v
            where v.event = :event
              and (v.voterRole = :voterRole
               or (:voterRole = com.votify.backend.entity.UserRole.PUBLIC and v.voterRole is null))
            group by v.participant.teamName
            order by count(v) desc, v.participant.teamName asc
            """)
    List<VoteTallyProjection> tallyByEventAndVoterRole(EventEntity event, UserRole voterRole);

    // Cuenta votos por rol de votante.
    @Query("""
            select count(v)
            from VoteEntity v
            where v.event = :event
              and (v.voterRole = :voterRole
               or (:voterRole = com.votify.backend.entity.UserRole.PUBLIC and v.voterRole is null))
            """)
    long countByEventAndVoterRole(EventEntity event, UserRole voterRole);

    long countByEvent(EventEntity event);
    List<VoteEntity> findAllByEventIsNull();

    // Indica si un usuario ya tiene votos registrados.
    boolean existsByUserId(Long userId);
    boolean existsByEventAndUserId(EventEntity event, Long userId);
}
