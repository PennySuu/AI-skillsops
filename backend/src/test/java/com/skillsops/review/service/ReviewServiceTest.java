package com.skillsops.review.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.review.domain.ReviewRecord;
import com.skillsops.review.mapper.ReviewRecordMapper;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillStatus;
import com.skillsops.skill.mapper.AuditRecordMapper;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRecordMapper reviewRecordMapper;

    @Mock
    private SkillMapper skillMapper;

    @Mock
    private AuditRecordMapper auditRecordMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void shouldApprovePendingReview() {
        when(currentUserService.requireUserId(request)).thenReturn(2L);
        ReviewRecord review = new ReviewRecord(11L, 100L, "pending", null, 1L, null, LocalDateTime.now(), LocalDateTime.now());
        when(reviewRecordMapper.findById(11L)).thenReturn(review);

        reviewService.approve(11L, request);

        verify(currentUserService).requireAdmin(request);
        verify(skillMapper).updateStatus(100L, SkillStatus.published.name());
        verify(reviewRecordMapper).updateStatus(11L, "approved", null, 2L);
    }

    @Test
    void shouldRejectAlreadyHandledReview() {
        when(currentUserService.requireUserId(request)).thenReturn(2L);
        when(reviewRecordMapper.findById(11L))
                .thenReturn(new ReviewRecord(11L, 100L, "approved", null, 1L, 2L, LocalDateTime.now(), LocalDateTime.now()));

        BusinessException ex = assertThrows(BusinessException.class, () -> reviewService.reject(11L, "reason reason", request));

        assertEquals(ErrorCode.OPERATION_FAILED, ex.getErrorCode());
        verify(skillMapper, never()).updateStatus(100L, SkillStatus.draft.name());
    }

    @Test
    void shouldOfflineSkillByAdmin() {
        when(currentUserService.requireUserId(request)).thenReturn(2L);
        when(skillMapper.findById(88L))
                .thenReturn(new Skill(88L, 1L, 1L, "n", "d", "https://x", SkillStatus.published, LocalDateTime.now(), LocalDateTime.now()));

        reviewService.offlineSkill(88L, "违规内容，管理员下架处理", request);

        verify(skillMapper).updateStatus(88L, SkillStatus.offline.name());
        verify(auditRecordMapper).insert(88L, "skill.offline", 2L, "ADMIN", "违规内容，管理员下架处理");
    }
}
