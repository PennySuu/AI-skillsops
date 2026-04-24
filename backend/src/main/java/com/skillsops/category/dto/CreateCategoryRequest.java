package com.skillsops.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(@NotBlank @Size(max = 64) String name, boolean enabled) {
}
