package com.skillsops.market.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.market.dto.MarketSkillSummaryDTO;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.mapper.SkillVersionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketQueryServiceTest {

    @Mock
    private SkillMapper skillMapper;

    @Mock
    private SkillVersionMapper skillVersionMapper;

    @InjectMocks
    private MarketQueryService marketQueryService;

    @Test
    void shouldQueryPublishedWithCategoryAndSort() {
        when(skillMapper.listPublished(anyInt(), anyInt(), eq("abc"), eq(10L), eq("name"), eq("asc")))
                .thenReturn(List.of(new MarketSkillSummaryDTO(1L, "A", "D", 0.0, 0)));
        when(skillMapper.countPublished(eq("abc"), eq(10L))).thenReturn(1L);

        var page = marketQueryService.listPublished(0, 10, "10", "abc", "name,asc");

        assertEquals(1, page.items().size());
        verify(skillMapper).listPublished(0, 10, "abc", 10L, "name", "asc");
        verify(skillMapper).countPublished("abc", 10L);
    }

    @Test
    void shouldRejectInvalidCategory() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> marketQueryService.listPublished(0, 10, "x", null, null));
        assertEquals(ErrorCode.VALIDATION_FAILED, ex.getErrorCode());
    }
}
