package com.votify.backend.dto;

import java.time.Instant;

// Comentario emitido sobre un equipo y visible para su propietario.
public record TeamCommentResponse(
        String authorLabel,
        String comment,
        Instant createdAt
) {
}
