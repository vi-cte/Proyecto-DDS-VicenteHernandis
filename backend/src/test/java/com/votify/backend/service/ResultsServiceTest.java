package com.votify.backend.service;

import com.votify.backend.dto.ResultItemResponse;
import com.votify.backend.dto.ResultsResponse;
import com.votify.backend.exception.ApiException;
import com.votify.backend.factory.PublicVoteCreator;
import com.votify.backend.repository.UserRepository;
import com.votify.backend.repository.VoteJpaRepository;
import com.votify.backend.repository.VoteTallyProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultsServiceTest {

    @Mock
    private VoteJpaRepository voteRepository;

    @Mock
    private ParticipantService participantService;

    @Mock
    private PublicVoteCreator voteCreator;

    @Mock
    private EventSettingsService eventSettingsService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VoteService voteService;

    @Test
    void shouldThrowExceptionWhenResultsAreHidden() {
        // Arrange: Resultados configurados como ocultos
        when(eventSettingsService.areResultsVisible()).thenReturn(false);

        // Act & Assert: Se debe denegar el acceso
        ApiException exception = assertThrows(ApiException.class, () -> voteService.getResults());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        
        // Verificamos que nunca se llama a la base de datos para contar votos
        verify(voteRepository, never()).tally();
    }

    @Test
    void shouldReturnAccurateResultsWhenResultsAreVisible() {
        // Arrange: Resultados visibles y simulamos respuesta de la base de datos
        when(eventSettingsService.areResultsVisible()).thenReturn(true);
        when(voteRepository.count()).thenReturn(150L);

        VoteTallyProjection p1 = mock(VoteTallyProjection.class);
        when(p1.getTeamName()).thenReturn("Equipo A");
        when(p1.getVotes()).thenReturn(100L);

        VoteTallyProjection p2 = mock(VoteTallyProjection.class);
        when(p2.getTeamName()).thenReturn("Equipo B");
        when(p2.getVotes()).thenReturn(50L);

        when(voteRepository.tally()).thenReturn(List.of(p1, p2));

        // Act: Solicitamos los resultados
        ResultsResponse response = voteService.getResults();

        // Assert: Agrupación y mapeo correctos
        assertNotNull(response);
        assertEquals(150L, response.totalVotes());
        assertEquals(2, response.results().size());
        
        // Comprobamos el correcto volcado de la información y la gestión de diferentes puntuaciones (y empates)
        ResultItemResponse result1 = response.results().get(0);
        assertEquals("Equipo A", result1.teamName());
        assertEquals(100L, result1.votes());
        
        ResultItemResponse result2 = response.results().get(1);
        assertEquals("Equipo B", result2.teamName());
        assertEquals(50L, result2.votes());
    }
}