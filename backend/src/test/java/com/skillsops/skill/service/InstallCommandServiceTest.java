package com.skillsops.skill.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillStatus;
import com.skillsops.skill.dto.InstallCommandResponse;
import com.skillsops.skill.mapper.SkillMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstallCommandServiceTest {

    @Mock
    private SkillMapper skillMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ObjectProvider<org.springframework.data.redis.core.StringRedisTemplate> redisTemplateProvider;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private InstallCommandService installCommandService;

    @Test
    void shouldRejectOfflineSkill() {
        when(currentUserService.requireUserId(request)).thenReturn(100L);
        when(skillMapper.findById(1L)).thenReturn(new Skill(
                1L, 2L, 3L, "n", "d", "u", SkillStatus.offline, LocalDateTime.now(), LocalDateTime.now()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> installCommandService.issueInstallCommand(1L, "idem-1", request));
        assertEquals(ErrorCode.SKILL_OFFLINE_NOT_INSTALLABLE, ex.getErrorCode());
    }

    @Test
    void shouldReturnSameCommandForSameIdempotencyKey() {
        when(currentUserService.requireUserId(request)).thenReturn(100L);
        when(skillMapper.findById(2L)).thenReturn(new Skill(
                2L, 2L, 3L, "n", "d", "u", SkillStatus.published, LocalDateTime.now(), LocalDateTime.now()));

        InstallCommandResponse first = installCommandService.issueInstallCommand(2L, "idem-2", request);
        InstallCommandResponse second = installCommandService.issueInstallCommand(2L, "idem-2", request);

        assertEquals(first.command(), second.command());
    }
}
