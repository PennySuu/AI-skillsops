package com.skillsops.rating.mapper;

import com.skillsops.rating.dto.SkillRatingSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RatingMapper {

    int upsert(
            @Param("userId") Long userId,
            @Param("skillId") Long skillId,
            @Param("score") Integer score,
            @Param("comment") String comment);

    SkillRatingSummary summarizeBySkillId(@Param("skillId") Long skillId);
}
