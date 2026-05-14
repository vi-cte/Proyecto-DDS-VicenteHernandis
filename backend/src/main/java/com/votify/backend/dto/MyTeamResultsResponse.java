package com.votify.backend.dto;

import java.util.List;

// Resumen privado del equipo del usuario autenticado en la pantalla de resultados.
public record MyTeamResultsResponse(
        String teamName,
        long votes,
        int position,
        long commentsCount,
        List<TeamCommentResponse> comments
) {
}
