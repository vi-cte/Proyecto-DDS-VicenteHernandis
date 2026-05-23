package com.votify.backend.entity;

// Fases de ciclo de vida soportadas por un evento.
public enum EventPhase {
    REGISTRATION_OPEN,
    REGISTRATION_CLOSED,
    PUBLIC_VOTING_OPEN,
    JURY_VOTING_OPEN,
    PUBLIC_AND_JURY_VOTING_OPEN,
    VOTING_CLOSED,
    RESULTS_VISIBLE,
    ARCHIVED
}
