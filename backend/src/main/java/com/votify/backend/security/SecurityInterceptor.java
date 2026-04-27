package com.votify.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.votify.backend.exception.ErrorResponse;
import com.votify.backend.repository.UserRepository;
import com.votify.backend.service.EventSettingsService;
import com.votify.backend.service.VoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Instant;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final UserRepository userRepository;
    private final EventSettingsService eventSettingsService;
    private final VoteService voteService;
    private final String adminPassword;

    public SecurityInterceptor(
            UserRepository userRepository,
            EventSettingsService eventSettingsService,
            VoteService voteService,
            @Value("${votify.admin.password:admin123}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.eventSettingsService = eventSettingsService;
        this.voteService = voteService;
        this.adminPassword = adminPassword;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (requiresAdmin(path, method)) {
            return validateAdmin(request, response);
        }
        if (requiresAuthenticatedParticipantLookup(path, method)) {
            return validateAuthenticatedUser(request, response);
        }
        if (requiresRegistrationAccess(path, method)) {
            return validateRegistrationAccess(request, response);
        }
        if (requiresVotingAccess(path, method)) {
            return validateVotingAccess(request, response);
        }
        if (requiresVisibleResults(path, method)) {
            return validateVisibleResults(request, response);
        }
        return true;
    }

    private boolean requiresAdmin(String path, String method) {
        return path.startsWith("/api/admin");
    }

    private boolean requiresRegistrationAccess(String path, String method) {
        if (!path.startsWith("/api/participants")) {
            return false;
        }
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method);
    }

    private boolean requiresAuthenticatedParticipantLookup(String path, String method) {
        return "/api/participants/mine".equals(path) && "GET".equalsIgnoreCase(method);
    }

    private boolean requiresVotingAccess(String path, String method) {
        if (!path.startsWith("/api/votes")) {
            return false;
        }
        return "POST".equalsIgnoreCase(method) || "GET".equalsIgnoreCase(method);
    }

    private boolean requiresVisibleResults(String path, String method) {
        return "/api/results".equals(path) && "GET".equalsIgnoreCase(method);
    }

    private boolean validateAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String providedPassword = request.getHeader("X-Admin-Password");
        if (providedPassword == null || providedPassword.isBlank()) {
            writeError(response, request, HttpStatus.UNAUTHORIZED, "Debes proporcionar la contraseña de administrador");
            return false;
        }
        if (!adminPassword.equals(providedPassword)) {
            writeError(response, request, HttpStatus.FORBIDDEN, "Contraseña de administrador incorrecta");
            return false;
        }
        return true;
    }

    private boolean validateRegistrationAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = extractUserId(request, response);
        if (userId == null) {
            return false;
        }
        if (!eventSettingsService.areRegistrationsOpen()) {
            writeError(response, request, HttpStatus.FORBIDDEN, "Las inscripciones están cerradas actualmente");
            return false;
        }
        return true;
    }

    private boolean validateAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return extractUserId(request, response) != null;
    }

    private boolean validateVotingAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = extractUserId(request, response);
        if (userId == null) {
            return false;
        }
        if (!eventSettingsService.isVotingOpen()) {
            writeError(response, request, HttpStatus.FORBIDDEN, "Las votaciones están cerradas actualmente");
            return false;
        }
        if ("POST".equalsIgnoreCase(request.getMethod()) && voteService.hasUserVoted(userId)) {
            writeError(response, request, HttpStatus.CONFLICT, "Ya has votado en este evento");
            return false;
        }
        return true;
    }

    private boolean validateVisibleResults(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!eventSettingsService.areResultsVisible()) {
            writeError(response, request, HttpStatus.FORBIDDEN, "Los resultados están ocultos actualmente por el administrador");
            return false;
        }
        return true;
    }

    private @Nullable Long extractUserId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rawUserId = request.getHeader("X-User-ID");
        if (rawUserId == null || rawUserId.isBlank()) {
            writeError(response, request, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para realizar esta acción");
            return null;
        }
        try {
            long userId = Long.parseLong(rawUserId);
            if (!userRepository.existsById(userId)) {
                writeError(response, request, HttpStatus.UNAUTHORIZED, "La sesión de usuario no es válida");
                return null;
            }
            return userId;
        } catch (NumberFormatException e) {
            writeError(response, request, HttpStatus.BAD_REQUEST, "El identificador de usuario no es válido");
            return null;
        }
    }

    private void writeError(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        response.getWriter().write(MAPPER.writeValueAsString(error));
    }
}
