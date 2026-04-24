package com.skillsops.ops.service;

import com.skillsops.ops.dto.Granularity;
import com.skillsops.ops.dto.OpsActiveAuthorDTO;
import com.skillsops.ops.dto.OpsTopSkillDTO;
import com.skillsops.ops.dto.OpsTrendPointDTO;
import com.skillsops.ops.mapper.OpsDashboardMapper;
import com.skillsops.skill.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpsDashboardServiceTest {

    @Mock
    private OpsDashboardMapper opsDashboardMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OpsDashboardService opsDashboardService;

    @Test
    void shouldBuildDashboardForWeekGranularity() {
        when(opsDashboardMapper.countPublishedSkills()).thenReturn(12L);
        when(opsDashboardMapper.countInstallsInDays(30)).thenReturn(25L);
        when(opsDashboardMapper.countRatingsInDays(30)).thenReturn(18L);
        when(opsDashboardMapper.installTrend(30, "week"))
                .thenReturn(List.of(new OpsTrendPointDTO("2026-W17", 5L)));
        when(opsDashboardMapper.topSkills(30))
                .thenReturn(List.of(new OpsTopSkillDTO(1L, "A", 10L)));
        when(opsDashboardMapper.activeAuthors(30))
                .thenReturn(List.of(new OpsActiveAuthorDTO(101L, "alice", 3L)));

        var result = opsDashboardService.dashboard(Granularity.week, 30, request);

        verify(currentUserService).requireAdmin(request);
        assertEquals(Granularity.week, result.granularity());
        assertEquals(30, result.days());
        assertEquals(3, result.metrics().size());
        assertEquals(1, result.installTrend().size());
        assertEquals(1, result.topSkills().size());
        assertEquals(1, result.activeAuthors().size());
    }
}
