package com.votify.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/*ParticipantEntity mapea la tabla participants: datos del equipo (nombre único, email, dirección, teléfono) 
y su lista de miembros en tabla participant_members como @ElementCollection.*/

@Entity
@Table(name = "participants")
public class ParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventEntity event;

    @Column(name = "team_name", nullable = false, length = 120)
    private String teamName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(length = 10485760) // Soporta hasta ~10MB en Base64
    private String logo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "participant_members", joinColumns = @JoinColumn(name = "participant_id"))
    @Column(name = "member_name", length = 120)
    private List<String> members = new ArrayList<>();

    @Column(name = "owner_email", length = 180)
    private String ownerEmail;

    // Devuelve el identificador generado del participante.
    public Long getId() {
        return id;
    }

    // Devuelve el evento al que pertenece el equipo.
    public EventEntity getEvent() {
        return event;
    }

    // Asigna el evento al que pertenece el equipo.
    public void setEvent(EventEntity event) {
        this.event = event;
    }

    // Devuelve el nombre del equipo participante.
    public String getTeamName() {
        return teamName;
    }

    // Actualiza el nombre del equipo participante.
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    // Devuelve el correo de contacto del equipo.
    public String getEmail() {
        return email;
    }

    // Actualiza el correo de contacto del equipo.
    public void setEmail(String email) {
        this.email = email;
    }

    // Devuelve el teléfono de contacto del equipo.
    public String getPhone() {
        return phone;
    }

    // Actualiza el teléfono de contacto del equipo.
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Devuelve la descripción del equipo.
    public String getDescription() {
        return description;
    }

    // Actualiza la descripción del equipo.
    public void setDescription(String description) {
        this.description = description;
    }

    // Devuelve el logo del equipo en Base64.
    public String getLogo() {
        return logo;
    }

    // Actualiza el logo del equipo en Base64.
    public void setLogo(String logo) {
        this.logo = logo;
    }

    // Devuelve la lista de miembros del equipo.
    public List<String> getMembers() {
        return members;
    }

    // Actualiza la lista de miembros evitando referencias externas mutables.
    public void setMembers(List<String> members) {
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
    }

    // Devuelve el correo del usuario propietario del equipo.
    public String getOwnerEmail() {
        return ownerEmail;
    }

    // Actualiza el correo del usuario propietario del equipo.
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
