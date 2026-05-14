package com.votify.backend.dto;

// Puntuacion otorgada por el jurado a un criterio concreto.
public record JuryCriterionScoreRequest(
        String criterion,
        Integer score
) {
}
