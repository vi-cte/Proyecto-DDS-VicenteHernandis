package com.votify.frontend.results;

import com.votify.frontend.dto.ResultItemResponse;
import com.votify.frontend.dto.ResultsResponse;

import java.util.List;

public record ResultsViewData(
        ResultsResponse response,
        List<ResultItemResponse> ranking,
        int participantCount
) {
    public ResultItemResponse winner() {
        return ranking.isEmpty() ? null : ranking.getFirst();
    }
}
