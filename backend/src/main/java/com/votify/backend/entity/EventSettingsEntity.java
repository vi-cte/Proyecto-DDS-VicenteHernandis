package com.votify.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_settings")
// Entidad singleton que guarda la configuración global del evento.
public class EventSettingsEntity {
    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(name = "registrations_open", nullable = false)
    private boolean registrationsOpen;

    @Column(name = "voting_open", nullable = false)
    private boolean votingOpen;

    @Column(name = "results_visible", nullable = false)
    private boolean resultsVisible;

    @Column(name = "max_teams_to_vote", nullable = false)
    private int maxTeamsToVote;

    // Devuelve el identificador fijo de la configuración.
    public Long getId() {
        return id;
    }

    // Asigna el identificador fijo de la configuración.
    public void setId(Long id) {
        this.id = id;
    }

    // Indica si las inscripciones están abiertas.
    public boolean isRegistrationsOpen() {
        return registrationsOpen;
    }

    // Actualiza el estado de apertura de inscripciones.
    public void setRegistrationsOpen(boolean registrationsOpen) {
        this.registrationsOpen = registrationsOpen;
    }

    // Indica si las votaciones están abiertas.
    public boolean isVotingOpen() {
        return votingOpen;
    }

    // Actualiza el estado de apertura de votaciones.
    public void setVotingOpen(boolean votingOpen) {
        this.votingOpen = votingOpen;
    }

    // Indica si los resultados son visibles.
    public boolean isResultsVisible() {
        return resultsVisible;
    }

    // Actualiza la visibilidad de los resultados.
    public void setResultsVisible(boolean resultsVisible) {
        this.resultsVisible = resultsVisible;
    }

    // Devuelve el máximo de equipos que puede votar un usuario.
    public int getMaxTeamsToVote() {
        return maxTeamsToVote;
    }

    // Actualiza el máximo de equipos que puede votar un usuario.
    public void setMaxTeamsToVote(int maxTeamsToVote) {
        this.maxTeamsToVote = maxTeamsToVote;
    }
}
