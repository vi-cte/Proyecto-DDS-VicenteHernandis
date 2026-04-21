package com.votify.backend.dto;

import java.util.List;

// DTO de salida con los datos de un participante.
public record ParticipantResponse(
        Long id,
        String teamName,
        String email,
        String phone,
        String description,
        String logo,
        List<String> members
) {
}
