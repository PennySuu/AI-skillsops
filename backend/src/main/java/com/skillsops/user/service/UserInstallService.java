package com.skillsops.user.service;

import com.skillsops.common.api.dto.PageResponse;
import com.skillsops.skill.mapper.InstallRecordMapper;
import com.skillsops.skill.service.CurrentUserService;
import com.skillsops.user.dto.UserInstallItemDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInstallService {

    private final CurrentUserService currentUserService;
    private final InstallRecordMapper installRecordMapper;

    public UserInstallService(CurrentUserService currentUserService, InstallRecordMapper installRecordMapper) {
        this.currentUserService = currentUserService;
        this.installRecordMapper = installRecordMapper;
    }

    public PageResponse<UserInstallItemDTO> listMyInstalls(int page, int size, HttpServletRequest request) {
        Long userId = currentUserService.requireUserId(request);
        int offset = page * size;
        List<UserInstallItemDTO> items = installRecordMapper.listByUser(userId, offset, size);
        long total = installRecordMapper.countByUser(userId);
        return new PageResponse<>(page, size, total, items);
    }
}
