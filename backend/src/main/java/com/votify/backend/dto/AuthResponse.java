package com.votify.backend.dto;

public record AuthResponse(
        String token,
        String email,
        String message
) {}