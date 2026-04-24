package com.skillsops.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSkillRequest(
        @NotBlank @Size(max = 128) String name,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 2048) String resourceUrl,
        @NotNull Long categoryId) {
}
