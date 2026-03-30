package com.votify.frontend.dto;

import java.util.List;

public class VoteRequest {
    private List<String> selections;

    public VoteRequest() {
    }

    public VoteRequest(List<String> selections) {
        this.selections = selections;
    }

    public List<String> getSelections() {
        return selections;
    }

    public void setSelections(List<String> selections) {
        this.selections = selections;
    }
}
