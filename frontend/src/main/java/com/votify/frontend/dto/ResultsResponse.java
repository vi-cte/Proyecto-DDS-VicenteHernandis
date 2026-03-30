package com.votify.frontend.dto;

import java.util.List;

public class ResultsResponse {
    private long totalVotes;
    private List<ResultItemResponse> results;

    public ResultsResponse() {
    }

    public long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(long totalVotes) {
        this.totalVotes = totalVotes;
    }

    public List<ResultItemResponse> getResults() {
        return results;
    }

    public void setResults(List<ResultItemResponse> results) {
        this.results = results;
    }
}
