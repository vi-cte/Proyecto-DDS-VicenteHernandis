package com.votify.backend.observer;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
// Sujeto observable que avisa a sus observadores cuando cambian los votos.
public class VoteEventPublisher {
    // Lista segura para altas/bajas mientras se notifican eventos desde peticiones concurrentes.
    private final List<VoteObserver> observers = new CopyOnWriteArrayList<>();

    // Registra un observador interesado en cambios de votación.
    public void addObserver(VoteObserver observer) {
        observers.add(observer);
    }

    // Elimina el observador cuando ya no debe recibir notificaciones.
    public void removeObserver(VoteObserver observer) {
        observers.remove(observer);
    }

    // Avisa a todos los observadores de que los resultados de un evento han cambiado.
    public void notifyVotesChanged(Long eventId) {
        observers.forEach(observer -> observer.onVotesChanged(eventId));
    }
}
