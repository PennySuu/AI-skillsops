package com.skillsops.skill.domain;

import java.time.LocalDateTime;

public record Skill(
        Long id,
        Long authorId,
        Long categoryId,
        String name,
        String description,
        String resourceUrl,
        SkillStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
