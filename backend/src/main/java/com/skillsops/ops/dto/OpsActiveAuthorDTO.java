package com.skillsops.ops.dto;

public record OpsActiveAuthorDTO(
        Long authorId,
        String username,
        long publishedCount) {
}
