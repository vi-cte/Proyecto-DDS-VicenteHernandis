package com.votify.backend.builder;

import com.votify.backend.entity.ParticipantEntity;
import java.util.List;
import org.springframework.lang.NonNull;

// Builder concreto que guarda los datos necesarios para crear un participante.
public class DefaultParticipantBuilder implements ParticipantBuilder {
    private String teamName;
    private String email;
    private String phone;
    private String description;
    private String logo;
    private List<String> members;
    private String ownerEmail;

    @Override
    // Asigna el nombre del equipo al participante en construcción.
    public ParticipantBuilder teamName(String teamName) {
        this.teamName = teamName;
        return this;
    }

    @Override
    // Asigna el correo de contacto al participante en construcción.
    public ParticipantBuilder email(String email) {
        this.email = email;
        return this;
    }

    @Override
    // Asigna el teléfono al participante en construcción.
    public ParticipantBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    @Override
    // Asigna la descripción del equipo al participante en construcción.
    public ParticipantBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    // Asigna el logo en Base64 al participante en construcción.
    public ParticipantBuilder logo(String logo) {
        this.logo = logo;
        return this;
    }

    @Override
    // Asigna la lista de miembros al participante en construcción.
    public ParticipantBuilder members(List<String> members) {
        this.members = members;
        return this;
    }

    @Override
    // Asigna el correo del usuario propietario al participante en construcción.
    public ParticipantBuilder ownerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
        return this;
    }

    @Override
    // Crea la entidad participante final con los datos acumulados.
    @NonNull
    public ParticipantEntity build() {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setTeamName(teamName);
        participant.setEmail(email);
        participant.setPhone(phone);
        participant.setDescription(description);
        participant.setLogo(logo);
        participant.setMembers(members);
        participant.setOwnerEmail(ownerEmail);
        return participant;
    }
}
