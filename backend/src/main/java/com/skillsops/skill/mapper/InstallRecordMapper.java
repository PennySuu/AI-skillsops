package com.skillsops.skill.mapper;

import com.skillsops.user.dto.UserInstallItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InstallRecordMapper {

    int upsert(
            @Param("userId") Long userId,
            @Param("skillId") Long skillId,
            @Param("installedVersion") String installedVersion);

    List<UserInstallItemDTO> listByUser(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size);

    long countByUser(@Param("userId") Long userId);
}
