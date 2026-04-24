package com.skillsops.skill.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.market.service.MarketCacheService;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillStatus;
import com.skillsops.skill.dto.UpdateSkillRequest;
import com.skillsops.skill.mapper.AuditRecordMapper;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.mapper.SkillVersionMapper;
import com.skillsops.review.mapper.ReviewRecordMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillLifecycleServiceTest {

    @Mock
    private SkillMapper skillMapper;

    @Mock
    private AuditRecordMapper auditRecordMapper;

    @Mock
    private SkillVersionMapper skillVersionMapper;

    @Mock
    private ReviewRecordMapper reviewRecordMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private MarketCacheService marketCacheService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SkillLifecycleService skillLifecycleService;

    @Test
    void shouldRejectUpdateWhenSkillIsPending() {
        when(currentUserService.requireUserId(request)).thenReturn(1L);
        when(currentUserService.requireRole(request)).thenReturn("USER");
        when(skillMapper.findById(100L)).thenReturn(new Skill(
                100L,
                1L,
                10L,
                "name",
                "desc",
                "https://example.com",
                SkillStatus.pending,
                LocalDateTime.now(),
                LocalDateTime.now()));

        assertThrows(BusinessException.class,
                () -> skillLifecycleService.updateSkill(100L, new UpdateSkillRequest("n", null, null), request));

        verify(skillMapper, never()).updateDraft(anyLong(), anyString(), anyString(), anyString());
        verify(auditRecordMapper, never()).insert(anyLong(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    void shouldRejectSubmitReviewWhenResourceUrlMissing() {
        when(currentUserService.requireUserId(request)).thenReturn(1L);
        when(currentUserService.requireRole(request)).thenReturn("USER");
        when(skillMapper.findById(101L)).thenReturn(new Skill(
                101L,
                1L,
                10L,
                "name",
                "desc",
                " ",
                SkillStatus.draft,
                LocalDateTime.now(),
                LocalDateTime.now()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> skillLifecycleService.submitReview(101L, request));

        assertEquals(ErrorCode.SKILL_RESOURCE_URL_REQUIRED, ex.getErrorCode());
        verify(skillMapper, never()).updateStatus(anyLong(), anyString());
    }
}
