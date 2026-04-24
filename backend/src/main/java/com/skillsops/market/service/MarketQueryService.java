package com.skillsops.market.service;

import com.skillsops.common.api.dto.PageResponse;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.market.dto.MarketSkillDetailDTO;
import com.skillsops.market.dto.MarketSkillSummaryDTO;
import com.skillsops.market.dto.SkillVersionDTO;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillVersion;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.mapper.SkillVersionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class MarketQueryService {

    private final SkillMapper skillMapper;
    private final SkillVersionMapper skillVersionMapper;
    private final MarketCacheService marketCacheService;

    public MarketQueryService(
            SkillMapper skillMapper,
            SkillVersionMapper skillVersionMapper,
            MarketCacheService marketCacheService) {
        this.skillMapper = skillMapper;
        this.skillVersionMapper = skillVersionMapper;
        this.marketCacheService = marketCacheService;
    }

    public PageResponse<MarketSkillSummaryDTO> listPublished(
            int page,
            int size,
            String category,
            String q,
            String sort) {
        int offset = page * size;
        Long categoryId = parseCategory(category);
        SortParam sortParam = parseSort(sort);
        String cacheKey = buildListCacheKey(page, size, q, categoryId, sortParam);
        var cached = marketCacheService.getPublishedList(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        List<MarketSkillSummaryDTO> items = skillMapper.listPublished(
                offset,
                size,
                q,
                categoryId,
                sortParam.field(),
                sortParam.direction());
        long total = skillMapper.countPublished(q, categoryId);
        PageResponse<MarketSkillSummaryDTO> result = new PageResponse<>(page, size, total, items);
        marketCacheService.cachePublishedList(cacheKey, result);
        return result;
    }

    public MarketSkillDetailDTO getPublishedSkillDetail(Long skillId) {
        var cached = marketCacheService.getPublishedDetail(skillId);
        if (cached.isPresent()) {
            return cached.get();
        }
        Skill skill = skillMapper.findPublishedById(skillId);
        if (skill == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Skill 不存在或未上架");
        }
        List<SkillVersionDTO> versions = skillVersionMapper.listBySkillId(skillId).stream()
                .map(this::mapVersion)
                .toList();
        MarketSkillDetailDTO result = new MarketSkillDetailDTO(skill.id(), skill.name(), skill.description(), versions);
        marketCacheService.cachePublishedDetail(skillId, result);
        return result;
    }

    private SkillVersionDTO mapVersion(SkillVersion value) {
        return new SkillVersionDTO(value.version(), value.createdAt().toString());
    }

    private Long parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(category);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "category 必须为数字");
        }
    }

    private SortParam parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return new SortParam("updatedAt", "desc");
        }
        String[] values = sort.split(",", 2);
        if (values.length != 2) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "sort 参数格式错误");
        }
        String field = values[0];
        String direction = values[1].toLowerCase(Locale.ROOT);
        boolean fieldValid = "updatedAt".equals(field) || "name".equals(field);
        boolean directionValid = "asc".equals(direction) || "desc".equals(direction);
        if (!fieldValid || !directionValid) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "sort 参数不支持");
        }
        return new SortParam(field, direction);
    }

    private String buildListCacheKey(int page, int size, String q, Long categoryId, SortParam sortParam) {
        return "p=" + page
                + ":s=" + size
                + ":q=" + (q == null ? "" : q)
                + ":c=" + (categoryId == null ? "" : categoryId)
                + ":sf=" + sortParam.field()
                + ":sd=" + sortParam.direction();
    }

    private record SortParam(String field, String direction) {
    }
}
