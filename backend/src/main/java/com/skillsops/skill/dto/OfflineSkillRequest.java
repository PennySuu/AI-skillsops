package com.skillsops.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OfflineSkillRequest(@NotBlank @Size(min = 10, max = 200) String reason) {
}
