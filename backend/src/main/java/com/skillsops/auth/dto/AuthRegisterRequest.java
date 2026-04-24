package com.skillsops.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank @Size(min = 3, max = 32) @Pattern(regexp = "^[A-Za-z0-9_\\-]+$") String username,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
