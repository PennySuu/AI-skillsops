package com.skillsops.skill.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InstallRecordMapper {

    int upsert(
            @Param("userId") Long userId,
            @Param("skillId") Long skillId,
            @Param("installedVersion") String installedVersion);
}
