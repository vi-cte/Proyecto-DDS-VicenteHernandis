package com.votify.backend.builder;

import com.votify.backend.entity.ParticipantEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParticipantBuilderTest {

    @Test
    void directorShouldConstructParticipantCorrectly() {
        // Arrange: Preparamos el builder y el director reales
        DefaultParticipantBuilder builder = new DefaultParticipantBuilder();
        ParticipantDirector director = new ParticipantDirector(builder);

        // Act: Ejecutamos la construcción
        ParticipantEntity entity = director.buildParticipant(
                "Equipo Beta", "beta@test.com", "987654321", "Descripción", null, List.of("M1"), "owner@test.com"
        );

        // Assert: Verificamos que el producto final tiene todas sus piezas en su sitio
        assertNotNull(entity, "La entidad construida no debe ser nula");
        assertEquals("Equipo Beta", entity.getTeamName());
        assertEquals("beta@test.com", entity.getEmail());
        assertEquals("987654321", entity.getPhone());
        assertEquals("Descripción", entity.getDescription());
        assertEquals("owner@test.com", entity.getOwnerEmail());
        assertEquals(1, entity.getMembers().size());
    }
}