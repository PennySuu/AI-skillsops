package com.skillsops.skill.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditRecordMapper {

    int insert(
            @Param("skillId") Long skillId,
            @Param("action") String action,
            @Param("actorId") Long actorId,
            @Param("actorRole") String actorRole,
            @Param("detail") String detail);
}
