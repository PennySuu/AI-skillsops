package com.skillsops.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSkillVersionRequest(
        @NotBlank @Pattern(regexp = "^[0-9]+\\.[0-9]+\\.[0-9]+$") String version,
        @NotBlank @Size(max = 1000) String changelog,
        @NotBlank @Size(max = 2048) String resourceUrl) {
}
