package com.skillsops.review.dto;

import java.time.LocalDateTime;

public record PendingReviewItem(
        Long reviewId,
        Long skillId,
        Long submittedBy,
        LocalDateTime createdAt) {
}
