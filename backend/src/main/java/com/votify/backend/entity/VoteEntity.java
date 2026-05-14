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
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventEntity event;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "voter_role")
    private UserRole voterRole = UserRole.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_category")
    private VoteCategory voteCategory = VoteCategory.PUBLIC_WINNER;

    @Column(name = "criterion_key", length = 120)
    private String criterionKey;

    @Column(name = "score_value")
    private Integer scoreValue;

    @Column(name = "comment_text", columnDefinition = "TEXT")
    private String commentText;

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

    // Devuelve el evento asociado al voto.
    public EventEntity getEvent() {
        return event;
    }

    // Asigna el evento asociado al voto.
    public void setEvent(EventEntity event) {
        this.event = event;
    }

    // Devuelve el identificador del usuario que votó.
    public Long getUserId() {
        return userId;
    }

    // Asigna el identificador del usuario que votó.
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Devuelve el rol del usuario que emitió el voto.
    public UserRole getVoterRole() {
        return voterRole == null ? UserRole.PUBLIC : voterRole;
    }

    // Asigna el rol del usuario que emitió el voto.
    public void setVoterRole(UserRole voterRole) {
        this.voterRole = voterRole == null ? UserRole.PUBLIC : voterRole;
    }

    // Devuelve la categoría del voto.
    public VoteCategory getVoteCategory() {
        return voteCategory == null ? VoteCategory.PUBLIC_WINNER : voteCategory;
    }

    // Asigna la categoría del voto.
    public void setVoteCategory(VoteCategory voteCategory) {
        this.voteCategory = voteCategory == null ? VoteCategory.PUBLIC_WINNER : voteCategory;
    }

    // Devuelve el criterio puntuado, si existe.
    public String getCriterionKey() {
        return criterionKey;
    }

    // Asigna el criterio puntuado.
    public void setCriterionKey(String criterionKey) {
        this.criterionKey = criterionKey;
    }

    // Devuelve la puntuacion asociada al voto, si existe.
    public Integer getScoreValue() {
        return scoreValue;
    }

    // Asigna la puntuacion asociada al voto.
    public void setScoreValue(Integer scoreValue) {
        this.scoreValue = scoreValue;
    }

    // Devuelve el comentario opcional asociado al voto.
    public String getCommentText() {
        return commentText;
    }

    // Asigna el comentario opcional asociado al voto.
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    // Devuelve la fecha de creación del voto.
    public Instant getCreatedAt() {
        return createdAt;
    }
}
