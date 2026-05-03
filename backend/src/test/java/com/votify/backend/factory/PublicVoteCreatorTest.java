package com.votify.backend.factory;

import com.votify.backend.domain.vote.PublicVote;
import com.votify.backend.domain.vote.Vote;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicVoteCreatorTest {

    @Test
    void factoryShouldCreateCorrectVoteType() {
        // Arrange: Instanciamos la factoría
        PublicVoteCreator creator = new PublicVoteCreator();

        // Act: Le pedimos que fabrique un producto
        Vote vote = creator.orderVote("Equipo Alpha");

        // Assert: Verificamos el tipo polimórfico y su estado inicial
        assertNotNull(vote, "El voto creado no debe ser nulo");
        assertTrue(vote instanceof PublicVote, "La factoría debe devolver una instancia de PublicVote");
        assertEquals("Equipo Alpha", vote.getOption(), "El voto debe inicializarse con la opción correcta");
    }
}