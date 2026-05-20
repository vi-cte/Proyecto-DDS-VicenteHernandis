package com.votify.backend.observer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoteEventPublisherTest {

    @Test
    void shouldNotifyRegisteredObserversWhenVotesChange() {
        VoteEventPublisher publisher = new VoteEventPublisher();
        List<Long> receivedEventIds = new ArrayList<>();

        publisher.addObserver(receivedEventIds::add);
        publisher.notifyVotesChanged(42L);

        assertEquals(List.of(42L), receivedEventIds);
    }

    @Test
    void shouldNotNotifyObserverAfterItIsRemoved() {
        VoteEventPublisher publisher = new VoteEventPublisher();
        List<Long> receivedEventIds = new ArrayList<>();
        VoteObserver observer = receivedEventIds::add;

        publisher.addObserver(observer);
        publisher.removeObserver(observer);
        publisher.notifyVotesChanged(42L);

        assertEquals(List.of(), receivedEventIds);
    }
}
