package com.skillsops.skill.dto;

import jakarta.validation.constraints.Size;

public record UpdateSkillRequest(
        @Size(max = 128) String name,
        @Size(max = 2000) String description,
        @Size(max = 2048) String resourceUrl) {
}
