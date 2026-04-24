package com.skillsops.rating.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.market.service.MarketCacheService;
import com.skillsops.rating.dto.UpsertRatingRequest;
import com.skillsops.rating.mapper.RatingMapper;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillStatus;
import com.skillsops.skill.mapper.InstallRecordMapper;
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
class RatingServiceTest {

    @Mock
    private RatingMapper ratingMapper;

    @Mock
    private InstallRecordMapper installRecordMapper;

    @Mock
    private SkillMapper skillMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private MarketCacheService marketCacheService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RatingService ratingService;

    @Test
    void shouldRejectRatingWhenNotInstalled() {
        when(currentUserService.requireUserId(request)).thenReturn(10L);
        when(skillMapper.findById(2L)).thenReturn(new Skill(
                2L, 1L, 1L, "n", "d", "u", SkillStatus.published, LocalDateTime.now(), LocalDateTime.now()));
        when(installRecordMapper.existsByUserAndSkill(10L, 2L)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> ratingService.upsertRating(2L, new UpsertRatingRequest(5, "good"), request));
        assertEquals(ErrorCode.RATING_REQUIRES_INSTALL, ex.getErrorCode());
        verify(ratingMapper, never()).upsert(10L, 2L, 5, "good");
    }

    @Test
    void shouldUpsertRatingAndEvictMarketCache() {
        when(currentUserService.requireUserId(request)).thenReturn(10L);
        when(skillMapper.findById(3L)).thenReturn(new Skill(
                3L, 1L, 1L, "n", "d", "u", SkillStatus.published, LocalDateTime.now(), LocalDateTime.now()));
        when(installRecordMapper.existsByUserAndSkill(10L, 3L)).thenReturn(true);

        ratingService.upsertRating(3L, new UpsertRatingRequest(4, null), request);

        verify(ratingMapper).upsert(10L, 3L, 4, null);
        verify(marketCacheService).evictListCache();
        verify(marketCacheService).evictSkillDetail(3L);
    }
}
