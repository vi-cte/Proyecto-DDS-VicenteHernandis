package com.votify.frontend.dto;

import java.util.List;

public class VoteResponse {
    private int recordedVotes;
    private List<String> selections;

    public VoteResponse() {
    }

    public int getRecordedVotes() {
        return recordedVotes;
    }

    public void setRecordedVotes(int recordedVotes) {
        this.recordedVotes = recordedVotes;
    }

    public List<String> getSelections() {
        return selections;
    }

    public void setSelections(List<String> selections) {
        this.selections = selections;
    }
}
