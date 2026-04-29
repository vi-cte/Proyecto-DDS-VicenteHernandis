package com.votify.backend.builder;

import com.votify.backend.entity.ParticipantEntity;
import java.util.List;
import org.springframework.lang.NonNull;

// Director que fija el orden de creación de un participante.
public class ParticipantDirector {
    private final ParticipantBuilder builder;

    // Recibe el builder concreto que realizará la construcción.
    public ParticipantDirector(ParticipantBuilder builder) {
        this.builder = builder;
    }

    // Construye un participante completo siguiendo la secuencia del patrón Builder.
    @NonNull
    public ParticipantEntity buildParticipant(
            String teamName,
            String email,
            String phone,
            String description,
            String logo,
            List<String> members,
            String ownerEmail
    ) {
        return builder
                .teamName(teamName)
                .email(email)
                .phone(phone)
                .description(description)
                .logo(logo)
                .members(members)
                .ownerEmail(ownerEmail)
                .build();
    }
}
