package com.skillsops.auth.mapper;

import com.skillsops.auth.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper {

    UserAccount findByUsername(@Param("username") String username);

    UserAccount findById(@Param("id") Long id);

    int insert(@Param("username") String username, @Param("passwordHash") String passwordHash, @Param("role") String role);
}
