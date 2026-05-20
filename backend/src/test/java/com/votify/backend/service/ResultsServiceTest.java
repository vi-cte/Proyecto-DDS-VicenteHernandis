package com.votify.backend.service;

import com.votify.backend.dto.ResultItemResponse;
import com.votify.backend.dto.ResultsResponse;
import com.votify.backend.exception.ApiException;
import com.votify.backend.factory.JuryVoteCreator;
import com.votify.backend.factory.PublicVoteCreator;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.UserRole;
import com.votify.backend.repository.UserRepository;
import com.votify.backend.repository.VoteJpaRepository;
import com.votify.backend.repository.VoteTallyProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
    private JuryVoteCreator juryVoteCreator;

    @Mock
    private EventSettingsService eventSettingsService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VoteService voteService;
    private final EventEntity activeEvent = new EventEntity();

    @BeforeEach
    void setUp() {
        activeEvent.setResultsVisible(true);
        lenient().when(eventSettingsService.getEventOrActive(null)).thenReturn(activeEvent);
    }

    @Test
    void shouldThrowExceptionWhenResultsAreHidden() {
        // Arrange: Resultados configurados como ocultos
        activeEvent.setResultsVisible(false);

        // Act & Assert: Se debe denegar el acceso
        ApiException exception = assertThrows(ApiException.class, () -> voteService.getResults());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        
        // Verificamos que nunca se llama a la base de datos para contar votos
        verify(voteRepository, never()).tallyByEventAndVoterRole(any(), any());
    }

    @Test
    void shouldReturnAccurateResultsWhenResultsAreVisible() {
        // Arrange: Resultados visibles y simulamos respuesta de la base de datos
        when(voteRepository.sumScoreByEventAndVoterRole(activeEvent, UserRole.PUBLIC)).thenReturn(150L);
        when(voteRepository.sumScoreByEventAndVoterRole(activeEvent, UserRole.JURY)).thenReturn(0L);

        VoteTallyProjection p1 = mock(VoteTallyProjection.class);
        when(p1.getTeamName()).thenReturn("Equipo A");
        when(p1.getVotes()).thenReturn(100L);

        VoteTallyProjection p2 = mock(VoteTallyProjection.class);
        when(p2.getTeamName()).thenReturn("Equipo B");
        when(p2.getVotes()).thenReturn(50L);

        when(voteRepository.tallyByEventAndVoterRole(activeEvent, UserRole.PUBLIC)).thenReturn(List.of(p1, p2));
        when(voteRepository.tallyByEventAndVoterRole(activeEvent, UserRole.JURY)).thenReturn(List.of());

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
