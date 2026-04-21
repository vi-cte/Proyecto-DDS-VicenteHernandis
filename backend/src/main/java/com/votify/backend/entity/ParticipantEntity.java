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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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

    @Column(name = "team_name", nullable = false, length = 120, unique = true)
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

    public Long getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members == null ? new ArrayList<>() : new ArrayList<>(members);
    }
}
