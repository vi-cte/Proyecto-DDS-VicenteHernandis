package com.votify.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/* VoteEntity mapea la tabla votes: cada voto referencia a un participante y guarda created_at al persistir. */

@Entity
@Table(name = "votes")
public class VoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private ParticipantEntity participant;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    // Rellena la fecha de creación justo antes de guardar el voto.
    void prePersist() {
        this.createdAt = Instant.now();
    }

    // Devuelve el identificador generado del voto.
    public Long getId() {
        return id;
    }

    // Devuelve el participante votado.
    public ParticipantEntity getParticipant() {
        return participant;
    }

    // Asigna el participante votado.
    public void setParticipant(ParticipantEntity participant) {
        this.participant = participant;
    }

    // Devuelve el identificador del usuario que votó.
    public Long getUserId() {
        return userId;
    }

    // Asigna el identificador del usuario que votó.
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Devuelve la fecha de creación del voto.
    public Instant getCreatedAt() {
        return createdAt;
    }
}
