package com.votify.backend.observer;

// Observador de cambios en las votaciones de un evento.
public interface VoteObserver {
    void onVotesChanged(Long eventId);
}
