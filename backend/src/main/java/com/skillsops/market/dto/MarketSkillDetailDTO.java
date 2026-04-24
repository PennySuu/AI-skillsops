package com.skillsops.market.dto;

import java.util.List;

public record MarketSkillDetailDTO(
        Long id,
        String name,
        String description,
        double avgRating,
        long ratingCount,
        List<SkillVersionDTO> versions) {
}
