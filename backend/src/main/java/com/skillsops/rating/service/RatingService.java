package com.skillsops.rating.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.market.service.MarketCacheService;
import com.skillsops.rating.dto.UpsertRatingRequest;
import com.skillsops.rating.mapper.RatingMapper;
import com.skillsops.skill.mapper.InstallRecordMapper;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {

    private final RatingMapper ratingMapper;
    private final InstallRecordMapper installRecordMapper;
    private final SkillMapper skillMapper;
    private final CurrentUserService currentUserService;
    private final MarketCacheService marketCacheService;

    public RatingService(
            RatingMapper ratingMapper,
            InstallRecordMapper installRecordMapper,
            SkillMapper skillMapper,
            CurrentUserService currentUserService,
            MarketCacheService marketCacheService) {
        this.ratingMapper = ratingMapper;
        this.installRecordMapper = installRecordMapper;
        this.skillMapper = skillMapper;
        this.currentUserService = currentUserService;
        this.marketCacheService = marketCacheService;
    }

    @Transactional
    public void upsertRating(Long skillId, UpsertRatingRequest request, HttpServletRequest httpServletRequest) {
        Long userId = currentUserService.requireUserId(httpServletRequest);
        if (skillMapper.findById(skillId) == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Skill 不存在");
        }
        if (!installRecordMapper.existsByUserAndSkill(userId, skillId)) {
            throw new BusinessException(ErrorCode.RATING_REQUIRES_INSTALL, "请先安装后再评分");
        }
        ratingMapper.upsert(userId, skillId, request.score(), request.comment());
        marketCacheService.evictListCache();
        marketCacheService.evictSkillDetail(skillId);
    }
}
