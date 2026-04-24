package com.skillsops.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
