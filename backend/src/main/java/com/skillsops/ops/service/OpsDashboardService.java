package com.skillsops.ops.service;

import com.skillsops.ops.dto.Granularity;
import com.skillsops.ops.dto.OpsDashboardDTO;
import com.skillsops.ops.dto.OpsMetricCardDTO;
import com.skillsops.ops.mapper.OpsDashboardMapper;
import com.skillsops.skill.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpsDashboardService {

    private final OpsDashboardMapper opsDashboardMapper;
    private final CurrentUserService currentUserService;

    public OpsDashboardService(OpsDashboardMapper opsDashboardMapper, CurrentUserService currentUserService) {
        this.opsDashboardMapper = opsDashboardMapper;
        this.currentUserService = currentUserService;
    }

    public OpsDashboardDTO dashboard(Granularity granularity, int days, HttpServletRequest request) {
        currentUserService.requireAdmin(request);
        long publishedSkills = opsDashboardMapper.countPublishedSkills();
        long installCount = opsDashboardMapper.countInstallsInDays(days);
        long ratingCount = opsDashboardMapper.countRatingsInDays(days);
        List<OpsMetricCardDTO> metrics = List.of(
                new OpsMetricCardDTO("publishedSkills", "已上架技能数", publishedSkills),
                new OpsMetricCardDTO("installCount", "近窗口安装数", installCount),
                new OpsMetricCardDTO("ratingCount", "近窗口评分数", ratingCount));

        return new OpsDashboardDTO(
                granularity,
                days,
                metrics,
                opsDashboardMapper.installTrend(days, granularity.name()),
                opsDashboardMapper.topSkills(days),
                opsDashboardMapper.activeAuthors(days));
    }
}
