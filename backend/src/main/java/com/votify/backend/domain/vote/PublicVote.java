package com.votify.backend.domain.vote;

public class PublicVote extends Vote {
    public PublicVote(String option) {
        this.option = option;
    }

    @Override
    public String voterRole() {
        return "PUBLIC";
    }
}
