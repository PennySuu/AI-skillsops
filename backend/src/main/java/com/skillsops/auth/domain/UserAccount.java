package com.skillsops.auth.domain;

import java.time.LocalDateTime;

public record UserAccount(
        Long id,
        String username,
        String passwordHash,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
