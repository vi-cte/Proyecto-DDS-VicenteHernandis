package com.votify.backend.builder;

import com.votify.backend.entity.ParticipantEntity;
import java.util.List;
import org.springframework.lang.NonNull;

// Define los pasos que debe implementar cualquier builder de participantes.
public interface ParticipantBuilder {
    // Recibe el nombre del equipo para la construcción.
    ParticipantBuilder teamName(String teamName);

    // Recibe el correo de contacto para la construcción.
    ParticipantBuilder email(String email);

    // Recibe el teléfono para la construcción.
    ParticipantBuilder phone(String phone);

    // Recibe la descripción del equipo para la construcción.
    ParticipantBuilder description(String description);

    // Recibe el logo en Base64 para la construcción.
    ParticipantBuilder logo(String logo);

    // Recibe los miembros del equipo para la construcción.
    ParticipantBuilder members(List<String> members);

    // Recibe el correo del propietario para la construcción.
    ParticipantBuilder ownerEmail(String ownerEmail);

    // Devuelve la entidad participante construida.
    @NonNull
    ParticipantEntity build();
}
