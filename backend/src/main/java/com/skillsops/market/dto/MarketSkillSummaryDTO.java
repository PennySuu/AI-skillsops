package com.skillsops.market.dto;

public record MarketSkillSummaryDTO(
        Long id,
        String name,
        String description,
        double avgRating,
        long ratingCount) {
}
