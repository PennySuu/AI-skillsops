package com.skillsops.skill.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.market.service.MarketCacheService;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillStatus;
import com.skillsops.skill.dto.CreateSkillRequest;
import com.skillsops.skill.dto.UpdateSkillRequest;
import com.skillsops.skill.mapper.AuditRecordMapper;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.mapper.SkillVersionMapper;
import com.skillsops.review.mapper.ReviewRecordMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillLifecycleService {

    private final SkillMapper skillMapper;
    private final SkillVersionMapper skillVersionMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final CurrentUserService currentUserService;
    private final MarketCacheService marketCacheService;

    public SkillLifecycleService(
            SkillMapper skillMapper,
            SkillVersionMapper skillVersionMapper,
            ReviewRecordMapper reviewRecordMapper,
            AuditRecordMapper auditRecordMapper,
            CurrentUserService currentUserService,
            MarketCacheService marketCacheService) {
        this.skillMapper = skillMapper;
        this.skillVersionMapper = skillVersionMapper;
        this.reviewRecordMapper = reviewRecordMapper;
        this.auditRecordMapper = auditRecordMapper;
        this.currentUserService = currentUserService;
        this.marketCacheService = marketCacheService;
    }

    @Transactional
    public void createDraft(CreateSkillRequest request, HttpServletRequest httpServletRequest) {
        Long actorId = currentUserService.requireUserId(httpServletRequest);
        skillMapper.insert(
                actorId,
                request.categoryId(),
                request.name(),
                request.description(),
                request.resourceUrl(),
                SkillStatus.draft.name());
    }

    @Transactional
    public void updateSkill(Long skillId, UpdateSkillRequest request, HttpServletRequest httpServletRequest) {
        Long actorId = currentUserService.requireUserId(httpServletRequest);
        String actorRole = currentUserService.requireRole(httpServletRequest);
        Skill skill = skillMapper.findById(skillId);
        if (skill == null) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "Skill 不存在");
        }
        if (skill.status() == SkillStatus.pending) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "审核中不可编辑，请等待审核结果");
        }

        skillMapper.updateDraft(skillId, request.name(), request.description(), request.resourceUrl());
        auditRecordMapper.insert(skillId, "skill.update", actorId, actorRole, "编辑 skill 内容");
        marketCacheService.evictListCache();
        marketCacheService.evictSkillDetail(skillId);
    }

    @Transactional
    public void submitReview(Long skillId, HttpServletRequest httpServletRequest) {
        Long actorId = currentUserService.requireUserId(httpServletRequest);
        String actorRole = currentUserService.requireRole(httpServletRequest);
        Skill skill = requireSkill(skillId);
        if (skill.resourceUrl() == null || skill.resourceUrl().isBlank()) {
            throw new BusinessException(ErrorCode.SKILL_RESOURCE_URL_REQUIRED, "resourceUrl 为必填项");
        }
        skillMapper.updateStatus(skillId, SkillStatus.pending.name());
        reviewRecordMapper.insertPending(skillId, actorId);
        auditRecordMapper.insert(skillId, "skill.submit_review", actorId, actorRole, "提交审核");
        marketCacheService.evictListCache();
        marketCacheService.evictSkillDetail(skillId);
    }

    @Transactional
    public void createVersion(Long skillId, String version, String changelog, String resourceUrl, HttpServletRequest request) {
        Long actorId = currentUserService.requireUserId(request);
        String actorRole = currentUserService.requireRole(request);
        requireSkill(skillId);
        try {
            skillVersionMapper.insert(skillId, version, changelog, resourceUrl);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.SKILL_VERSION_CONFLICT, "版本号冲突");
        }
        auditRecordMapper.insert(skillId, "skill.create_version", actorId, actorRole, "发布新版本 " + version);
        marketCacheService.evictListCache();
        marketCacheService.evictSkillDetail(skillId);
    }

    private Skill requireSkill(Long skillId) {
        Skill skill = skillMapper.findById(skillId);
        if (skill == null) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "Skill 不存在");
        }
        return skill;
    }
}
