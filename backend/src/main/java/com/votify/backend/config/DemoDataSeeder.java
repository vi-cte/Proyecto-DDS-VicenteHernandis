package com.votify.backend.config;

import com.votify.backend.entity.EventEntity;
import com.votify.backend.entity.EventPhase;
import com.votify.backend.entity.JuryVotingMode;
import com.votify.backend.entity.ParticipantEntity;
import com.votify.backend.entity.User;
import com.votify.backend.entity.UserRole;
import com.votify.backend.entity.VoteCategory;
import com.votify.backend.entity.VoteEntity;
import com.votify.backend.repository.EventJpaRepository;
import com.votify.backend.repository.ParticipantJpaRepository;
import com.votify.backend.repository.UserRepository;
import com.votify.backend.repository.VoteJpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "votify.seed", name = "enabled", havingValue = "true")
// Carga datos de demostracion realistas para enseñar el sistema completo.
public class DemoDataSeeder implements CommandLineRunner {
    // Evento marcador usado para detectar si el seed ya se ejecutó y evitar duplicados.
    private static final String MARKER_EVENT_NAME = "Demo Votify Live 2026";
    // Contraseña común de usuarios demo, guardada hasheada igual que en AuthService.
    private static final String DEMO_PASSWORD = "demo1234";

    private final EventJpaRepository eventRepository;
    private final ParticipantJpaRepository participantRepository;
    private final UserRepository userRepository;
    private final VoteJpaRepository voteRepository;

    public DemoDataSeeder(
            EventJpaRepository eventRepository,
            ParticipantJpaRepository participantRepository,
            UserRepository userRepository,
            VoteJpaRepository voteRepository
    ) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
    }

    @Override
    @Transactional
    // Punto de entrada del seed: crea datos solo cuando no existe el evento marcador.
    public void run(String... args) {
        boolean alreadySeeded = eventRepository.findAll().stream()
                .anyMatch(event -> MARKER_EVENT_NAME.equalsIgnoreCase(event.getName()));
        if (alreadySeeded) {
            System.out.println("Demo seed: datos ya existentes, no se duplican.");
            return;
        }

        // Usuarios y eventos se crean primero porque participantes y votos dependen de ellos.
        Map<String, User> users = createUsers();
        EventEntity liveEvent = createEvent(
                MARKER_EVENT_NAME,
                LocalDate.of(2026, 5, 22),
                "Evento activo con votación de jurado abierta, votos públicos acumulados y rankings en tiempo real.",
                EventPhase.PUBLIC_AND_JURY_VOTING_OPEN,
                3,
                true,
                JuryVotingMode.MULTICRITERIA
        );
        EventEntity publicEvent = createEvent(
                "Feria de Innovacion Campus 2026",
                LocalDate.of(2026, 6, 12),
                "Evento activo en fase de votación pública para enseñar cómo crece el ranking del público.",
                EventPhase.PUBLIC_VOTING_OPEN,
                3,
                true,
                JuryVotingMode.SIMPLE
        );
        EventEntity resultsEvent = createEvent(
                "Votify Awards 2025",
                LocalDate.of(2025, 11, 28),
                "Evento finalizado con resultados visibles para probar histórico y pantalla de resultados.",
                EventPhase.RESULTS_VISIBLE,
                3,
                true,
                JuryVotingMode.MULTICRITERIA
        );
        EventEntity archivedEvent = createEvent(
                "Reto Prototipos 2024",
                LocalDate.of(2024, 10, 8),
                "Evento archivado con datos de muestra para validar el histórico.",
                EventPhase.ARCHIVED,
                2,
                false,
                JuryVotingMode.SIMPLE
        );

        // Participantes repartidos por eventos para cubrir dashboard, votación, resultados e histórico.
        List<ParticipantEntity> liveTeams = createParticipants(liveEvent, List.of(
                team("Aurora Labs", "aurora@demo.votify", "IA para reducir tiempos de espera en hospitales."),
                team("EcoTrack", "ecotrack@demo.votify", "Trazabilidad de residuos para campus y empresas."),
                team("MindBridge", "mindbridge@demo.votify", "Acompañamiento digital para salud mental universitaria."),
                team("SafeRoute", "saferoute@demo.votify", "Rutas nocturnas seguras con datos colaborativos."),
                team("AgroPulse", "agropulse@demo.votify", "Sensores accesibles para agricultura de precisión."),
                team("CivicFlow", "civicflow@demo.votify", "Participación ciudadana para presupuestos locales.")
        ), users);
        List<ParticipantEntity> publicTeams = createParticipants(publicEvent, List.of(
                team("BlueNest", "bluenest@demo.votify", "Gestión energética para residencias."),
                team("SkillForge", "skillforge@demo.votify", "Microaprendizaje adaptativo para FP."),
                team("MediLink", "medilink@demo.votify", "Seguimiento simple de tratamientos crónicos."),
                team("ParkWise", "parkwise@demo.votify", "Predicción de aparcamiento urbano."),
                team("FoodLoop", "foodloop@demo.votify", "Marketplace contra el desperdicio alimentario.")
        ), users);
        List<ParticipantEntity> resultsTeams = createParticipants(resultsEvent, List.of(
                team("SolarByte", "solarbyte@demo.votify", "Optimización solar para edificios públicos."),
                team("AquaGuard", "aquaguard@demo.votify", "Detección temprana de fugas de agua."),
                team("LegalEase", "legalease@demo.votify", "Asistente para trámites legales básicos."),
                team("GreenMile", "greenmile@demo.votify", "Logística sostenible de última milla.")
        ), users);
        List<ParticipantEntity> archivedTeams = createParticipants(archivedEvent, List.of(
                team("Archive One", "archive.one@demo.votify", "Prototipo histórico de prueba."),
                team("Archive Two", "archive.two@demo.votify", "Segundo prototipo histórico.")
        ), users);

        // Votos de muestra diseñados para que los rankings tengan ganadores claros y empates realistas.
        createPublicVotes(liveEvent, liveTeams, users, List.of(
                votePlan("public01@demo.votify", "Aurora Labs", "EcoTrack", "MindBridge"),
                votePlan("public02@demo.votify", "Aurora Labs", "SafeRoute", "AgroPulse"),
                votePlan("public03@demo.votify", "EcoTrack", "Aurora Labs", "CivicFlow"),
                votePlan("public04@demo.votify", "Aurora Labs", "MindBridge", "SafeRoute"),
                votePlan("public05@demo.votify", "EcoTrack", "AgroPulse", "Aurora Labs"),
                votePlan("public06@demo.votify", "MindBridge", "Aurora Labs", "EcoTrack"),
                votePlan("public07@demo.votify", "SafeRoute", "Aurora Labs", "MindBridge"),
                votePlan("public08@demo.votify", "Aurora Labs", "EcoTrack", "SafeRoute"),
                votePlan("public09@demo.votify", "AgroPulse", "EcoTrack", "CivicFlow"),
                votePlan("public10@demo.votify", "Aurora Labs", "MindBridge", "EcoTrack"),
                votePlan("public11@demo.votify", "EcoTrack", "Aurora Labs", "AgroPulse"),
                votePlan("public12@demo.votify", "MindBridge", "SafeRoute", "Aurora Labs")
        ));
        createJuryMulticriteriaVotes(liveEvent, liveTeams, users, List.of("jury01@demo.votify", "jury02@demo.votify", "jury03@demo.votify"));

        createPublicVotes(publicEvent, publicTeams, users, List.of(
                votePlan("public13@demo.votify", "BlueNest", "FoodLoop", "SkillForge"),
                votePlan("public14@demo.votify", "MediLink", "BlueNest", "ParkWise"),
                votePlan("public15@demo.votify", "BlueNest", "SkillForge", "FoodLoop"),
                votePlan("public16@demo.votify", "FoodLoop", "MediLink", "BlueNest"),
                votePlan("public17@demo.votify", "SkillForge", "BlueNest", "ParkWise"),
                votePlan("public18@demo.votify", "BlueNest", "MediLink", "FoodLoop"),
                votePlan("public19@demo.votify", "ParkWise", "SkillForge", "BlueNest"),
                votePlan("public20@demo.votify", "FoodLoop", "BlueNest", "MediLink")
        ));
        createSimpleJuryVotes(publicEvent, publicTeams, users, List.of(
                votePlan("jury01@demo.votify", "MediLink", "BlueNest", "FoodLoop"),
                votePlan("jury02@demo.votify", "BlueNest", "SkillForge", "MediLink")
        ));

        createPublicVotes(resultsEvent, resultsTeams, users, List.of(
                votePlan("public01@demo.votify", "SolarByte", "AquaGuard", "GreenMile"),
                votePlan("public02@demo.votify", "AquaGuard", "SolarByte", "LegalEase"),
                votePlan("public03@demo.votify", "SolarByte", "GreenMile", "AquaGuard"),
                votePlan("public04@demo.votify", "LegalEase", "SolarByte", "AquaGuard"),
                votePlan("public05@demo.votify", "SolarByte", "AquaGuard", "LegalEase"),
                votePlan("public06@demo.votify", "GreenMile", "SolarByte", "AquaGuard")
        ));
        createJuryMulticriteriaVotes(resultsEvent, resultsTeams, users, List.of("jury01@demo.votify", "jury02@demo.votify"));

        createPublicVotes(archivedEvent, archivedTeams, users, List.of(
                votePlan("public07@demo.votify", "Archive One"),
                votePlan("public08@demo.votify", "Archive One"),
                votePlan("public09@demo.votify", "Archive Two")
        ));

        System.out.println("Demo seed: datos creados. Usuarios demo usan contraseña: " + DEMO_PASSWORD);
    }

    private Map<String, User> createUsers() {
        Map<String, User> users = new LinkedHashMap<>();
        addUser(users, "admin.demo@votify.local", UserRole.PUBLIC);
        for (int i = 1; i <= 20; i++) {
            addUser(users, "public%02d@demo.votify".formatted(i), UserRole.PUBLIC);
        }
        for (int i = 1; i <= 4; i++) {
            addUser(users, "jury%02d@demo.votify".formatted(i), UserRole.JURY);
        }
        return users;
    }

    // Crea o actualiza un usuario demo, manteniendo la operación repetible.
    private void addUser(Map<String, User> users, String email, UserRole role) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setEmail(email);
        user.setPassword(hashPassword(DEMO_PASSWORD));
        user.setRole(role);
        users.put(email, userRepository.save(user));
    }

    // Construye un evento completo en una fase concreta del ciclo de vida.
    private EventEntity createEvent(
            String name,
            LocalDate eventDate,
            String description,
            EventPhase phase,
            int maxTeamsToVote,
            boolean juryEnabled,
            JuryVotingMode juryVotingMode
    ) {
        EventEntity event = new EventEntity();
        event.setName(name);
        event.setEventDate(eventDate);
        event.setDescription(description);
        event.setMaxTeamsToVote(maxTeamsToVote);
        event.setJuryEnabled(juryEnabled);
        event.setJuryVotingMode(juryVotingMode);
        event.setPhase(phase);
        return eventRepository.save(event);
    }

    // Crea equipos con propietario, miembros y datos de contacto dentro del evento indicado.
    private List<ParticipantEntity> createParticipants(EventEntity event, List<TeamSeed> teams, Map<String, User> users) {
        List<ParticipantEntity> participants = new ArrayList<>();
        int memberSeed = 1;
        for (TeamSeed team : teams) {
            if (!users.containsKey(team.ownerEmail())) {
                addUser(users, team.ownerEmail(), UserRole.PUBLIC);
            }
            ParticipantEntity participant = new ParticipantEntity();
            participant.setEvent(event);
            participant.setTeamName(team.name());
            participant.setEmail(team.ownerEmail());
            participant.setPhone("+34 600 %03d %03d".formatted(memberSeed, memberSeed + 100));
            participant.setDescription(team.description());
            participant.setOwnerEmail(team.ownerEmail());
            participant.setMembers(List.of(
                    "Alex Demo " + memberSeed,
                    "Nora Demo " + memberSeed,
                    "Irene Demo " + memberSeed
            ));
            participants.add(participantRepository.save(participant));
            memberSeed++;
        }
        return participants;
    }

    // Inserta votos públicos simples; cada equipo nombrado recibe un voto del usuario indicado.
    private void createPublicVotes(EventEntity event, List<ParticipantEntity> participants, Map<String, User> users, List<VotePlan> plans) {
        for (VotePlan plan : plans) {
            User user = users.get(plan.userEmail());
            for (String teamName : plan.teamNames()) {
                ParticipantEntity participant = findParticipant(participants, teamName);
                voteRepository.save(vote(event, participant, user, UserRole.PUBLIC, VoteCategory.PUBLIC_WINNER, null, null, publicComment(teamName)));
            }
        }
    }

    // Inserta votos de jurado en modo simple para eventos que no usan criterios.
    private void createSimpleJuryVotes(EventEntity event, List<ParticipantEntity> participants, Map<String, User> users, List<VotePlan> plans) {
        for (VotePlan plan : plans) {
            User user = users.get(plan.userEmail());
            for (String teamName : plan.teamNames()) {
                ParticipantEntity participant = findParticipant(participants, teamName);
                voteRepository.save(vote(event, participant, user, UserRole.JURY, VoteCategory.JURY_WINNER, null, null, juryComment(teamName)));
            }
        }
    }

    // Genera evaluaciones multicriterio: cuatro votos por equipo, uno por cada criterio del jurado.
    private void createJuryMulticriteriaVotes(EventEntity event, List<ParticipantEntity> participants, Map<String, User> users, List<String> juryEmails) {
        String[] criteria = {"innovacion", "viabilidad", "impacto", "presentacion"};
        int juryIndex = 0;
        for (String juryEmail : juryEmails) {
            User jury = users.get(juryEmail);
            int teamIndex = 0;
            for (ParticipantEntity participant : participants) {
                int base = 6 + Math.floorMod(participant.getTeamName().hashCode() + juryIndex + teamIndex, 4);
                for (String criterion : criteria) {
                    int score = Math.min(10, base + ("presentacion".equals(criterion) ? 1 : 0));
                    voteRepository.save(vote(
                            event,
                            participant,
                            jury,
                            UserRole.JURY,
                            VoteCategory.JURY_MULTICRITERIA,
                            criterion,
                            score,
                            "innovacion".equals(criterion) ? "Evaluación sólida de " + participant.getTeamName() + "." : null
                    ));
                }
                teamIndex++;
            }
            juryIndex++;
        }
    }

    // Factoría común para mantener consistente la creación de entidades VoteEntity.
    private @NonNull VoteEntity vote(
            EventEntity event,
            ParticipantEntity participant,
            User user,
            UserRole voterRole,
            VoteCategory category,
            @Nullable String criterion,
            @Nullable Integer score,
            @Nullable String comment
    ) {
        VoteEntity vote = new VoteEntity();
        vote.setEvent(event);
        vote.setParticipant(participant);
        vote.setUserId(user.getId());
        vote.setVoterRole(voterRole);
        vote.setVoteCategory(category);
        vote.setCriterionKey(criterion);
        vote.setScoreValue(score);
        vote.setCommentText(comment);
        return vote;
    }

    // Localiza el participante por nombre dentro de la lista ya creada para ese evento.
    private ParticipantEntity findParticipant(List<ParticipantEntity> participants, String teamName) {
        return participants.stream()
                .filter(participant -> participant.getTeamName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe el equipo demo: " + teamName));
    }

    private String publicComment(String teamName) {
        return "Me convence el impacto de " + teamName + " y lo veo fácil de probar en un piloto.";
    }

    private String juryComment(String teamName) {
        return teamName + " destaca por claridad, ejecución y potencial de escalado.";
    }

    private VotePlan votePlan(String userEmail, String... teamNames) {
        return new VotePlan(userEmail, List.of(teamNames));
    }

    private TeamSeed team(String name, String ownerEmail, String description) {
        return new TeamSeed(name, ownerEmail, description);
    }

    // Reproduce el mismo hash de contraseñas usado por AuthService.
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo preparar la contraseña demo", e);
        }
    }

    private record TeamSeed(String name, String ownerEmail, String description) {
    }

    private record VotePlan(String userEmail, List<String> teamNames) {
    }
}
