package com.skillsops.ops.dto;

import java.util.List;

public record OpsDashboardDTO(
        Granularity granularity,
        int days,
        List<OpsMetricCardDTO> metrics,
        List<OpsTrendPointDTO> installTrend,
        List<OpsTopSkillDTO> topSkills,
        List<OpsActiveAuthorDTO> activeAuthors) {
}
