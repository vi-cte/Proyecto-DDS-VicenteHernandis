package com.votify.backend.service;

import com.votify.backend.dto.AdminEventResponse;
import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.EventPhase;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.UserRole;
import com.votify.backend.observer.VoteEventPublisher;
import com.votify.backend.repository.EventJpaRepository;
import com.votify.backend.repository.ParticipantJpaRepository;
import com.votify.backend.repository.VoteJpaRepository;
import com.votify.backend.repository.VoteTallyProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventAdminServiceTest {

    @Mock
    private EventJpaRepository eventRepository;

    @Mock
    private ParticipantJpaRepository participantRepository;

    @Mock
    private VoteJpaRepository voteRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private VoteEventPublisher voteEventPublisher;

    private EventAdminService eventAdminService;
    private EventEntity event;

    @BeforeEach
    void setUp() {
        eventAdminService = new EventAdminService(eventRepository, participantRepository, voteRepository, jdbcTemplate, voteEventPublisher);
        event = new EventEntity();
        event.setName("Evento dashboard");
        event.setMaxTeamsToVote(3);
        event.setPhase(EventPhase.RESULTS_VISIBLE);
    }

    @Test
    void shouldReturnSeparatedPublicAndJuryRankingsForAdminDashboard() {
        ParticipantEntity publicLeader = participant("Equipo Publico");
        ParticipantEntity juryLeader = participant("Equipo Jurado");
        ParticipantEntity withoutVotes = participant("Equipo Sin Votos");
        VoteTallyProjection publicTally = tally("Equipo Publico", 12L);
        VoteTallyProjection juryTally = tally("Equipo Jurado", 30L);

        when(eventRepository.findAllByOrderByActiveDescEventDateDescIdDesc()).thenReturn(List.of(event));
        when(participantRepository.findAllByEvent(event)).thenReturn(List.of(publicLeader, juryLeader, withoutVotes));
        when(participantRepository.countByEvent(event)).thenReturn(3L);
        when(voteRepository.tallyByEvent(event)).thenReturn(List.of(publicTally, juryTally));
        when(voteRepository.tallyByEventAndVoterRole(event, UserRole.PUBLIC)).thenReturn(List.of(publicTally));
        when(voteRepository.tallyByEventAndVoterRole(event, UserRole.JURY)).thenReturn(List.of(juryTally));
        when(voteRepository.sumScoreByEventAndVoterRole(event, UserRole.PUBLIC)).thenReturn(12L);
        when(voteRepository.sumScoreByEventAndVoterRole(event, UserRole.JURY)).thenReturn(30L);

        AdminEventResponse response = eventAdminService.findAll().getFirst();

        assertEquals(42L, response.totalVotes());
        assertEquals(3L, response.participants());
        assertEquals("Equipo Publico", response.publicRanking().getFirst().teamName());
        assertEquals(12L, response.publicRanking().getFirst().votes());
        assertEquals("Equipo Sin Votos", response.publicRanking().get(2).teamName());
        assertEquals(0L, response.publicRanking().get(2).votes());
        assertEquals("Equipo Jurado", response.juryRanking().getFirst().teamName());
        assertEquals(30L, response.juryRanking().getFirst().votes());
    }

    private ParticipantEntity participant(String teamName) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setTeamName(teamName);
        return participant;
    }

    private VoteTallyProjection tally(String teamName, Long votes) {
        VoteTallyProjection projection = mock(VoteTallyProjection.class);
        when(projection.getTeamName()).thenReturn(teamName);
        when(projection.getVotes()).thenReturn(votes);
        return projection;
    }
}
