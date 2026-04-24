package com.skillsops.ops.mapper;

import com.skillsops.ops.dto.OpsActiveAuthorDTO;
import com.skillsops.ops.dto.OpsTopSkillDTO;
import com.skillsops.ops.dto.OpsTrendPointDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OpsDashboardMapper {

    long countPublishedSkills();

    long countInstallsInDays(@Param("days") int days);

    long countRatingsInDays(@Param("days") int days);

    List<OpsTrendPointDTO> installTrend(
            @Param("days") int days,
            @Param("granularity") String granularity);

    List<OpsTopSkillDTO> topSkills(@Param("days") int days);

    List<OpsActiveAuthorDTO> activeAuthors(@Param("days") int days);
}
