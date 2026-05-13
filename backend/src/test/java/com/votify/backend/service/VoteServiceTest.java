package com.votify.backend.service;

import com.votify.backend.domain.vote.PublicVote;
import com.votify.backend.dto.VoteRequest;
import com.votify.backend.dto.VoteResponse;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.User;
import com.votify.backend.entity.UserRole;
import com.votify.backend.entity.VoteEntity;
import com.votify.backend.exception.ApiException;
import com.votify.backend.factory.JuryVoteCreator;
import com.votify.backend.factory.PublicVoteCreator;
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

    @InjectMocks
    private VoteService voteService;

    private VoteRequest validRequest;
    private final Long validUserId = 1L;
    private final String teamName = "Equipo Alpha";
    private User publicUser;

    @BeforeEach
    void setUp() {
        validRequest = new VoteRequest(List.of(teamName));
        publicUser = new User();
        publicUser.setEmail("publico@test.com");
        publicUser.setPassword("secret");
        publicUser.setRole(UserRole.PUBLIC);
    }

    @Test
    void shouldRegisterVoteSuccessfullyWhenAllConditionsAreMet() {
        // Arrange: Todas las precondiciones para el caso de éxito
        when(eventSettingsService.isVotingOpen()).thenReturn(true);
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(publicUser));
        when(voteRepository.existsByUserId(validUserId)).thenReturn(false);
        when(eventSettingsService.getMaxTeamsToVote()).thenReturn(3);
        when(participantService.existsByTeamName(teamName)).thenReturn(true);

        PublicVote dummyVote = new PublicVote(teamName);
        when(voteCreator.orderVote(teamName)).thenReturn(dummyVote);
        
        ParticipantEntity dummyParticipant = new ParticipantEntity();
        dummyParticipant.setTeamName(teamName);
        when(participantService.getByTeamName(teamName)).thenReturn(dummyParticipant);

        // Act: Emitimos el voto
        VoteResponse response = voteService.createVotes(validRequest, validUserId);

        // Assert: Verificamos que se ha registrado correctamente
        assertNotNull(response);
        assertEquals(1, response.recordedVotes());
        assertTrue(response.selections().contains(teamName));
        verify(voteRepository, times(1)).save(any(VoteEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenVotingIsClosed() {
        // Arrange: Votaciones cerradas
        when(eventSettingsService.isVotingOpen()).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(validRequest, validUserId));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotRegistered() {
        // Arrange: Votaciones abiertas, pero el usuario no existe en la base de datos
        when(eventSettingsService.isVotingOpen()).thenReturn(true);
        when(userRepository.findById(validUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(validRequest, validUserId));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserHasAlreadyVoted() {
        // Arrange: Votaciones abiertas y usuario registrado, pero el usuario YA ha votado antes
        when(eventSettingsService.isVotingOpen()).thenReturn(true);
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(publicUser));
        when(voteRepository.existsByUserId(validUserId)).thenReturn(true);

        // Act & Assert: Debe lanzar excepción de conflicto (HTTP 409)
        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(validRequest, validUserId));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("Ya has votado"));
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTeamIsNotRegistered() {
        // Arrange: Usuario válido y votaciones abiertas, pero intentamos votar a un equipo inexistente
        when(eventSettingsService.isVotingOpen()).thenReturn(true);
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(publicUser));
        when(voteRepository.existsByUserId(validUserId)).thenReturn(false);
        when(eventSettingsService.getMaxTeamsToVote()).thenReturn(3);
        
        // Simulamos explícitamente que el equipo no existe
        when(participantService.existsByTeamName(teamName)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> voteService.createVotes(validRequest, validUserId));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("El equipo no existe"));
        verify(voteRepository, never()).save(any());
    }
}
