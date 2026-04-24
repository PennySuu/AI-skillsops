package com.skillsops.category.mapper;

import com.skillsops.category.dto.CategoryItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<CategoryItemDTO> list(
            @Param("offset") int offset,
            @Param("size") int size);

    long countAll();

    int insert(
            @Param("name") String name,
            @Param("enabled") boolean enabled);

    int updateName(
            @Param("id") Long id,
            @Param("name") String name);

    int updateStatus(
            @Param("id") Long id,
            @Param("enabled") boolean enabled);

    CategoryItemDTO findById(@Param("id") Long id);

    CategoryItemDTO findByName(@Param("name") String name);
}
