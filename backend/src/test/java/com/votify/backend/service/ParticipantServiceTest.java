package com.votify.backend.service;

import com.votify.backend.dto.ParticipantRequest;
import com.votify.backend.dto.ParticipantResponse;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.User;
import com.votify.backend.exception.ApiException;
import com.votify.backend.repository.ParticipantJpaRepository;
import com.votify.backend.repository.UserRepository;
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
class ParticipantServiceTest {

    @Mock
    private ParticipantJpaRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventSettingsService eventSettingsService;

    @InjectMocks
    private ParticipantService participantService;

    private ParticipantRequest request;
    private User user;
    private EventEntity activeEvent;

    @BeforeEach
    void setUp() {
        // Preparamos los datos base para las pruebas
        request = new ParticipantRequest(
                "Equipo Alpha",
                "equipo@test.com",
                "123456789",
                "Un equipo de prueba",
                null,
                List.of("Miembro 1", "Miembro 2")
        );

        user = new User();
        user.setEmail("user@test.com");
        activeEvent = new EventEntity();
        activeEvent.setName("Evento test");
        activeEvent.setRegistrationsOpen(true);
        lenient().when(eventSettingsService.getEventOrActive(null)).thenReturn(activeEvent);
    }

    @Test
    void shouldPersistParticipantWhenRegistrationsAreOpen() {
        // Arrange: Simulamos que la fase de registro está ABIERTA
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(participantRepository.existsByEventAndTeamNameIgnoreCase(activeEvent, "Equipo Alpha")).thenReturn(false);
        when(participantRepository.findByEventAndOwnerEmailIgnoreCase(activeEvent, "user@test.com")).thenReturn(Optional.empty());

        ParticipantEntity savedEntity = new ParticipantEntity();
        savedEntity.setTeamName("Equipo Alpha");
        when(participantRepository.save(any(ParticipantEntity.class))).thenReturn(savedEntity);

        // Act: Intentamos crear el equipo
        ParticipantResponse response = participantService.create(request, 1L);

        // Assert: Se persiste exitosamente en BD
        assertNotNull(response);
        assertEquals("Equipo Alpha", response.teamName());
        verify(participantRepository, times(1)).save(any(ParticipantEntity.class)); // Verificamos que se llamó a "save"
    }

    @Test
    void shouldThrowExceptionAndNotPersistWhenRegistrationsAreClosed() {
        // Arrange: Simulamos que la fase de registro está CERRADA
        activeEvent.setRegistrationsOpen(false);

        // Act & Assert: Al intentar crear, debe lanzar excepción
        ApiException exception = assertThrows(ApiException.class, () -> participantService.create(request, 1L));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        
        // Verificamos de forma estricta que NUNCA se intentó guardar en BD
        verify(participantRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTeamNameAlreadyExists() {
        // Arrange: Simulamos que el registro está abierto y el usuario existe, pero el nombre del equipo ya existe
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(participantRepository.existsByEventAndTeamNameIgnoreCase(activeEvent, "Equipo Alpha")).thenReturn(true);

        // Act & Assert: Al intentar crear, debe lanzar excepción de conflicto
        ApiException exception = assertThrows(ApiException.class, () -> participantService.create(request, 1L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        
        // Verificamos de forma estricta que NUNCA se intentó guardar en BD
        verify(participantRepository, never()).save(any());
    }
}
