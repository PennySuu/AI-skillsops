package com.skillsops.skill.mapper;

import com.skillsops.skill.domain.Skill;
import com.skillsops.market.dto.MarketSkillSummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkillMapper {

    int insert(
            @Param("authorId") Long authorId,
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("description") String description,
            @Param("resourceUrl") String resourceUrl,
            @Param("status") String status);

    Skill findById(@Param("id") Long id);

    int updateDraft(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("resourceUrl") String resourceUrl);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    Skill findPublishedById(@Param("id") Long id);

    List<MarketSkillSummaryDTO> listPublished(
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("sortField") String sortField,
            @Param("sortDirection") String sortDirection);

    long countPublished(
            @Param("q") String q,
            @Param("categoryId") Long categoryId);
}
