package com.votify.backend.domain.vote;

public abstract class Vote {
    protected String option;

    public abstract String voterRole();

    public String getOption() {
        return option;
    }
}
