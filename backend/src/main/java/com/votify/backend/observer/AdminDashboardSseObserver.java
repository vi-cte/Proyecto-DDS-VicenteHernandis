package com.votify.backend.observer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
// Observador que transforma cambios de votos en eventos SSE para el dashboard admin.
public class AdminDashboardSseObserver implements VoteObserver {
    private static final long TIMEOUT = 0L;

    private final VoteEventPublisher voteEventPublisher;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public AdminDashboardSseObserver(VoteEventPublisher voteEventPublisher) {
        this.voteEventPublisher = voteEventPublisher;
    }

    @PostConstruct
    public void registerObserver() {
        voteEventPublisher.addObserver(this);
    }

    @PreDestroy
    public void unregisterObserver() {
        voteEventPublisher.removeObserver(this);
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        send(emitter, "dashboard-connected", null);
        return emitter;
    }

    @Override
    public void onVotesChanged(Long eventId) {
        for (SseEmitter emitter : emitters) {
            send(emitter, "dashboard-updated", eventId);
        }
    }

    private void send(SseEmitter emitter, String eventName, Long eventId) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(eventId == null ? "" : eventId.toString()));
        } catch (IOException | IllegalStateException e) {
            emitters.remove(emitter);
        }
    }
}
