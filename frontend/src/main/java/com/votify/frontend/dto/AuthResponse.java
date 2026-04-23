package com.votify.frontend.dto;

public record AuthResponse(String token, String email, String message) {
}