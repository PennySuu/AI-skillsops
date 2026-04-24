package com.skillsops.skill.domain;

import java.time.LocalDateTime;

public record AuditRecord(
        Long id,
        Long skillId,
        String action,
        Long actorId,
        String actorRole,
        String detail,
        LocalDateTime createdAt) {
}
