package com.skillsops.skill.mapper;

import com.skillsops.skill.domain.SkillVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkillVersionMapper {

    int insert(
            @Param("skillId") Long skillId,
            @Param("version") String version,
            @Param("changelog") String changelog,
            @Param("resourceUrl") String resourceUrl);

    SkillVersion findLatestBySkillId(@Param("skillId") Long skillId);

    List<SkillVersion> listBySkillId(@Param("skillId") Long skillId);
}
