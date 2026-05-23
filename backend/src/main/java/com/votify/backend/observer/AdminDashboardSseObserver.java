package com.votify.backend.observer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
// Observador que transforma cambios de votos en eventos SSE para el dashboard admin.
public class AdminDashboardSseObserver implements VoteObserver {
    // Mantiene el stream abierto indefinidamente mientras el cliente admin siga conectado.
    private static final long TIMEOUT = 0L;

    private final VoteEventPublisher voteEventPublisher;
    // Cada SseEmitter representa un dashboard admin conectado en tiempo real.
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public AdminDashboardSseObserver(VoteEventPublisher voteEventPublisher) {
        this.voteEventPublisher = voteEventPublisher;
    }

    @PostConstruct
    // Al arrancar Spring, esta clase se suscribe como observador del publisher de votos.
    public void registerObserver() {
        voteEventPublisher.addObserver(this);
    }

    @PreDestroy
    // Al cerrar la aplicación, se retira para no dejar referencias colgadas.
    public void unregisterObserver() {
        voteEventPublisher.removeObserver(this);
    }

    // Crea una suscripción SSE para un dashboard y registra limpieza automática al desconectar.
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        send(emitter, "dashboard-connected", null);
        return emitter;
    }

    @SuppressWarnings("null")
    @Override
    // Cuando hay nuevos votos, envía un evento ligero; el frontend decide recargar los datos.
    public void onVotesChanged(Long eventId) {
        for (SseEmitter emitter : emitters) {
            send(emitter, "dashboard-updated", eventId);
        }
    }

    // Envía un mensaje SSE y elimina clientes que ya no aceptan datos.
    @SuppressWarnings("null")
    private void send(@NonNull SseEmitter emitter, @NonNull String eventName, @Nullable Long eventId) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(eventId == null ? "" : eventId.toString()));
        } catch (IOException | IllegalStateException e) {
            emitters.remove(emitter);
        }
    }
}
