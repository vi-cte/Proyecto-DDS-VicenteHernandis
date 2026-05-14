package com.votify.frontend.results;

import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.dto.ResultsResponse;
import com.votify.frontend.dto.MyTeamResultsResponse;

import java.util.List;

// Agrupa los datos preparados para renderizar las vistas de resultados.
public record ResultsViewData(
        ResultsResponse response,
        List<ResultItemResponse> ranking,
        List<ResultItemResponse> juryRanking,
        int participantCount
) {
    // Devuelve el primer clasificado o null si no hay resultados.
    public ResultItemResponse winner() {
        return ranking.isEmpty() ? null : ranking.getFirst();
    }

    // Devuelve el primer clasificado del jurado o null si no hay resultados.
    public ResultItemResponse juryWinner() {
        return juryRanking.isEmpty() ? null : juryRanking.getFirst();
    }

    // Devuelve el resumen del equipo del usuario autenticado, si existe.
    public MyTeamResultsResponse myTeam() {
        return response.getMyTeam();
    }
}
