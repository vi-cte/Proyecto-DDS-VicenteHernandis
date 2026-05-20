package com.votify.backend.service;

import com.votify.backend.domain.vote.PublicVote;
import com.votify.backend.dto.JuryCriterionScoreRequest;
import com.votify.backend.dto.JuryTeamEvaluationRequest;
import com.votify.backend.dto.ParticipantResponse;
import com.votify.backend.dto.VoteRequest;
import com.votify.backend.dto.VoteResponse;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.EventPhase;
import com.votify.backend.entity.JuryVotingMode;
import com.votify.backend.entity.User;
import com.votify.backend.entity.UserRole;
import com.votify.backend.entity.VoteCategory;
import com.votify.backend.entity.VoteEntity;
import com.votify.backend.exception.ApiException;
import com.votify.backend.factory.JuryVoteCreator;
import com.votify.backend.factory.PublicVoteCreator;
import com.votify.backend.observer.VoteEventPublisher;
import com.votify.backend.repository.UserRepository;
import com.votify.backend.repository.VoteJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class VoteServiceTest {

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

    @Mock
    private VoteEventPublisher voteEventPublisher;

    @InjectMocks
    private VoteService voteService;

    private VoteRequest validRequest;
    private final Long validUserId = 1L;
    private final String teamName = "Equipo Alpha";
    private User publicUser;
    private User juryUser;
    private EventEntity activeEvent;

    @BeforeEach
    void setUp() {
        validRequest = new VoteRequest(List.of(teamName));
        publicUser = new User();
        publicUser.setEmail("publico@test.com");
        publicUser.setPassword("secret");
        publicUser.setRole(UserRole.PUBLIC);
        juryUser = new User();
        juryUser.setEmail("jury@test.com");
        juryUser.setPassword("secret");
        juryUser.setRole(UserRole.JURY);
        activeEvent = new EventEntity();
        activeEvent.setName("Evento test");
        activeEvent.setPhase(EventPhase.PUBLIC_VOTING_OPEN);
        activeEvent.setMaxTeamsToVote(3);
        lenient().when(eventSettingsService.getEventOrActive(null)).thenReturn(activeEvent);
    }

    @Test
    void shouldRegisterVoteSuccessfullyWhenAllConditionsAreMet() {
        // Arrange: Todas las precondiciones para el caso de éxito
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(publicUser));
        when(voteRepository.existsByEventAndUserId(activeEvent, validUserId)).thenReturn(false);
        when(participantService.existsByTeamName(teamName, null)).thenReturn(true);

        PublicVote dummyVote = new PublicVote(teamName);
        when(voteCreator.orderVote(teamName)).thenReturn(dummyVote);
        
        ParticipantEntity dummyParticipant = new ParticipantEntity();
        dummyParticipant.setTeamName(teamName);
        when(participantService.getByTeamName(teamName, null)).thenReturn(dummyParticipant);

        // Act: Emitimos el voto
        VoteResponse response = voteService.createVotes(validRequest, validUserId);

        // Assert: Verificamos que se ha registrado correctamente
        assertNotNull(response);
        assertEquals(1, response.recordedVotes());
        assertTrue(response.selections().contains(teamName));
        verify(voteRepository, times(1)).save(any(VoteEntity.class));
        verify(voteEventPublisher).notifyVotesChanged(activeEvent.getId());
    }

    @Test
    void shouldThrowExceptionWhenVotingIsClosed() {
        // Arrange: Votaciones cerradas
        activeEvent.setPhase(EventPhase.VOTING_CLOSED);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(validRequest, validUserId));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(voteRepository, never()).save(any());
        verify(voteEventPublisher, never()).notifyVotesChanged(any());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotRegistered() {
        // Arrange: Votaciones abiertas, pero el usuario no existe en la base de datos
        when(userRepository.findById(validUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(validRequest, validUserId));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserHasAlreadyVoted() {
        // Arrange: Votaciones abiertas y usuario registrado, pero el usuario YA ha votado antes
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(publicUser));
        when(voteRepository.existsByEventAndUserId(activeEvent, validUserId)).thenReturn(true);

        // Act & Assert: Debe lanzar excepción de conflicto (HTTP 409)
        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(validRequest, validUserId));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("Ya has votado"));
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTeamIsNotRegistered() {
        // Arrange: Usuario válido y votaciones abiertas, pero intentamos votar a un equipo inexistente
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(publicUser));
        when(voteRepository.existsByEventAndUserId(activeEvent, validUserId)).thenReturn(false);
        
        // Simulamos explícitamente que el equipo no existe
        when(participantService.existsByTeamName(teamName, null)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(validRequest, validUserId));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("El equipo no existe"));
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldRegisterCompleteJuryMulticriteriaEvaluationForAllTeams() {
        activeEvent.setJuryEnabled(true);
        activeEvent.setJuryVotingMode(JuryVotingMode.MULTICRITERIA);
        activeEvent.setPhase(EventPhase.JURY_VOTING_OPEN);
        String secondTeam = "Equipo Beta";
        VoteRequest request = multicriteriaRequest(List.of(teamName, secondTeam));

        when(userRepository.findById(validUserId)).thenReturn(Optional.of(juryUser));
        when(participantService.findAll(null)).thenReturn(List.of(
                new ParticipantResponse(1L, teamName, "a@test.com", null, null, null, List.of()),
                new ParticipantResponse(2L, secondTeam, "b@test.com", null, null, null, List.of())
        ));
        when(voteRepository.countDistinctParticipantsByEventAndUserIdAndVoteCategory(activeEvent, validUserId, VoteCategory.JURY_MULTICRITERIA))
                .thenReturn(0L);
        when(participantService.getByTeamName(eq(teamName), isNull())).thenReturn(participant(teamName));
        when(participantService.getByTeamName(eq(secondTeam), isNull())).thenReturn(participant(secondTeam));

        VoteResponse response = voteService.createVotes(request, validUserId);

        assertEquals(8, response.recordedVotes());
        assertEquals(List.of(teamName, secondTeam), response.selections());
        verify(voteRepository).saveAll(argThat(votes -> {
            int count = 0;
            for (VoteEntity vote : votes) {
                assertNotNull(vote);
                count++;
            }
            return count == 8;
        }));
        verify(voteRepository, never()).save(any());
        verify(voteEventPublisher).notifyVotesChanged(activeEvent.getId());
    }

    @Test
    void shouldRejectJuryMulticriteriaEvaluationWhenAnyTeamIsMissing() {
        activeEvent.setJuryEnabled(true);
        activeEvent.setJuryVotingMode(JuryVotingMode.MULTICRITERIA);
        activeEvent.setPhase(EventPhase.JURY_VOTING_OPEN);
        String secondTeam = "Equipo Beta";
        VoteRequest request = multicriteriaRequest(List.of(teamName));

        when(userRepository.findById(validUserId)).thenReturn(Optional.of(juryUser));
        when(participantService.findAll(null)).thenReturn(List.of(
                new ParticipantResponse(1L, teamName, "a@test.com", null, null, null, List.of()),
                new ParticipantResponse(2L, secondTeam, "b@test.com", null, null, null, List.of())
        ));

        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(request, validUserId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("todos los equipos"));
        verify(voteRepository, never()).save(any());
        verify(voteRepository, never()).saveAll(any());
        verify(voteEventPublisher, never()).notifyVotesChanged(any());
    }

    private VoteRequest multicriteriaRequest(List<String> teamNames) {
        List<JuryTeamEvaluationRequest> evaluations = teamNames.stream()
                .map(name -> new JuryTeamEvaluationRequest(name, List.of(
                        new JuryCriterionScoreRequest("innovacion", 8),
                        new JuryCriterionScoreRequest("viabilidad", 7),
                        new JuryCriterionScoreRequest("presentacion", 9),
                        new JuryCriterionScoreRequest("impacto", 6)
                ), null))
                .toList();
        return new VoteRequest(List.of(), List.of(), null, null, null, null, null, List.of(), evaluations, null);
    }

    private ParticipantEntity participant(String name) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setTeamName(name);
        return participant;
    }
}
