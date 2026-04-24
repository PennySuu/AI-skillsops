package com.skillsops.user.service;

import com.skillsops.skill.mapper.InstallRecordMapper;
import com.skillsops.skill.service.CurrentUserService;
import com.skillsops.user.dto.UserInstallItemDTO;
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
class UserInstallServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private InstallRecordMapper installRecordMapper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserInstallService userInstallService;

    @Test
    void shouldReturnPagedInstallsWithFlags() {
        when(currentUserService.requireUserId(request)).thenReturn(7L);
        when(installRecordMapper.listByUser(7L, 0, 10)).thenReturn(List.of(
                new UserInstallItemDTO(1L, "Skill-A", "2026-04-24T10:00:00", "1.0.0", "1.1.0", true, false)));
        when(installRecordMapper.countByUser(7L)).thenReturn(1L);

        var page = userInstallService.listMyInstalls(0, 10, request);

        assertEquals(1, page.total());
        assertEquals(1, page.items().size());
        assertEquals(true, page.items().get(0).updateAvailable());
        verify(installRecordMapper).listByUser(7L, 0, 10);
    }
}
