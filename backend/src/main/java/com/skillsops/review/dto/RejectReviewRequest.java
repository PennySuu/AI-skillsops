package com.skillsops.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectReviewRequest(@NotBlank @Size(min = 10, max = 200) String reason) {
}
