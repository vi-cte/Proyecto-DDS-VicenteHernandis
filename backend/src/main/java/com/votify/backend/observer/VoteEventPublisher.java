package com.votify.backend.observer;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
// Sujeto observable que avisa a sus observadores cuando cambian los votos.
public class VoteEventPublisher {
    private final List<VoteObserver> observers = new CopyOnWriteArrayList<>();

    public void addObserver(VoteObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(VoteObserver observer) {
        observers.remove(observer);
    }

    public void notifyVotesChanged(Long eventId) {
        observers.forEach(observer -> observer.onVotesChanged(eventId));
    }
}
