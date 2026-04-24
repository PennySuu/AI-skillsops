package com.skillsops.review.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.market.service.MarketCacheService;
import com.skillsops.review.domain.ReviewRecord;
import com.skillsops.review.dto.PendingReviewItem;
import com.skillsops.review.mapper.ReviewRecordMapper;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillStatus;
import com.skillsops.skill.mapper.AuditRecordMapper;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRecordMapper reviewRecordMapper;
    private final SkillMapper skillMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final CurrentUserService currentUserService;
    private final MarketCacheService marketCacheService;

    public ReviewService(
            ReviewRecordMapper reviewRecordMapper,
            SkillMapper skillMapper,
            AuditRecordMapper auditRecordMapper,
            CurrentUserService currentUserService,
            MarketCacheService marketCacheService) {
        this.reviewRecordMapper = reviewRecordMapper;
        this.skillMapper = skillMapper;
        this.auditRecordMapper = auditRecordMapper;
        this.currentUserService = currentUserService;
        this.marketCacheService = marketCacheService;
    }

    public List<PendingReviewItem> listPending(HttpServletRequest request) {
        currentUserService.requireAdmin(request);
        return reviewRecordMapper.listPending();
    }

    @Transactional
    public void approve(Long reviewId, HttpServletRequest request) {
        Long reviewerId = currentUserService.requireUserId(request);
        currentUserService.requireAdmin(request);
        ReviewRecord review = requirePendingReview(reviewId);
        skillMapper.updateStatus(review.skillId(), SkillStatus.published.name());
        reviewRecordMapper.updateStatus(reviewId, "approved", null, reviewerId);
        auditRecordMapper.insert(review.skillId(), "review.approve", reviewerId, "ADMIN", "审核通过");
        marketCacheService.evictListCache();
        marketCacheService.evictSkillDetail(review.skillId());
    }

    @Transactional
    public void reject(Long reviewId, String reason, HttpServletRequest request) {
        Long reviewerId = currentUserService.requireUserId(request);
        currentUserService.requireAdmin(request);
        ReviewRecord review = requirePendingReview(reviewId);
        skillMapper.updateStatus(review.skillId(), SkillStatus.draft.name());
        reviewRecordMapper.updateStatus(reviewId, "rejected", reason, reviewerId);
        auditRecordMapper.insert(review.skillId(), "review.reject", reviewerId, "ADMIN", reason);
        marketCacheService.evictListCache();
        marketCacheService.evictSkillDetail(review.skillId());
    }

    @Transactional
    public void offlineSkill(Long skillId, String reason, HttpServletRequest request) {
        Long reviewerId = currentUserService.requireUserId(request);
        currentUserService.requireAdmin(request);
        Skill skill = skillMapper.findById(skillId);
        if (skill == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Skill 不存在");
        }
        skillMapper.updateStatus(skillId, SkillStatus.offline.name());
        auditRecordMapper.insert(skillId, "skill.offline", reviewerId, "ADMIN", reason);
        marketCacheService.evictListCache();
        marketCacheService.evictSkillDetail(skillId);
    }

    private ReviewRecord requirePendingReview(Long reviewId) {
        ReviewRecord review = reviewRecordMapper.findById(reviewId);
        if (review == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "审核记录不存在");
        }
        if (!"pending".equals(review.status())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该审核记录已处理");
        }
        return review;
    }
}
