package com.skillsops.review.domain;

import java.time.LocalDateTime;

public record ReviewRecord(
        Long id,
        Long skillId,
        String status,
        String reason,
        Long submittedBy,
        Long reviewedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
