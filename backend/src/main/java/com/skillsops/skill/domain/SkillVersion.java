package com.skillsops.skill.domain;

import java.time.LocalDateTime;

public record SkillVersion(
        Long id,
        Long skillId,
        String version,
        String changelog,
        String resourceUrl,
        LocalDateTime createdAt) {
}
