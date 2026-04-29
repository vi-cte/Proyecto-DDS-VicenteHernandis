package com.votify.frontend.dto;

import java.util.List;

// DTO de frontend usado para enviar altas y actualizaciones de participantes.
public class ParticipantRequest {
    private String teamName;
    private String email;
    private String phone;
    private String description;
    private String logo;
    private List<String> members;

    // Constructor vacío requerido por Jackson.
    public ParticipantRequest() {
    }

    // Crea una petición de participante con todos sus campos.
    public ParticipantRequest(String teamName, String email, String phone, String description, String logo, List<String> members) {
        this.teamName = teamName;
        this.email = email;
        this.phone = phone;
        this.description = description;
        this.logo = logo;
        this.members = members;
    }

    // Devuelve el nombre del equipo.
    public String getTeamName() {
        return teamName;
    }

    // Actualiza el nombre del equipo.
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    // Devuelve el correo de contacto.
    public String getEmail() {
        return email;
    }

    // Actualiza el correo de contacto.
    public void setEmail(String email) {
        this.email = email;
    }

    // Devuelve el teléfono de contacto.
    public String getPhone() {
        return phone;
    }

    // Actualiza el teléfono de contacto.
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

    // Devuelve el logo en Base64.
    public String getLogo() {
        return logo;
    }

    // Actualiza el logo en Base64.
    public void setLogo(String logo) {
        this.logo = logo;
    }

    // Devuelve la lista de miembros.
    public List<String> getMembers() {
        return members;
    }

    // Actualiza la lista de miembros.
    public void setMembers(List<String> members) {
        this.members = members;
    }
}
