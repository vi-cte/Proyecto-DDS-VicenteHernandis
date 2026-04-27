package com.votify.frontend.client;

public record AccessDecision(boolean allowed, String message) {
    public static AccessDecision allow() {
        return new AccessDecision(true, "");
    }

    public static AccessDecision deny(String message) {
        return new AccessDecision(false, message);
    }
}
