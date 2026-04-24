package com.skillsops.auth.dto;

public record AuthProfileResponse(Long userId, String username, String role, long expiresInSeconds) {
}
