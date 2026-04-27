package com.votify.backend.dto;

public record EventSettingsDto(
        boolean registrationsOpen,
        boolean votingOpen,
        boolean resultsVisible,
        int maxTeamsToVote
) {}