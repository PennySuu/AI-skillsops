package com.skillsops.skill.service;

import com.skillsops.auth.service.AuthService;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    public Long requireUserId(HttpServletRequest request) {
        HttpSession session = requireSession(request);
        Object userId = session.getAttribute(AuthService.SESSION_USER_ID);
        if (!(userId instanceof Long uid)) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED, "登录态已失效，请重新登录");
        }
        return uid;
    }

    public String requireRole(HttpServletRequest request) {
        HttpSession session = requireSession(request);
        Object role = session.getAttribute(AuthService.SESSION_USER_ROLE);
        if (!(role instanceof String roleValue)) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED, "登录态已失效，请重新登录");
        }
        return roleValue;
    }

    public void requireAdmin(HttpServletRequest request) {
        String role = requireRole(request);
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "仅管理员可执行该操作");
        }
    }

    private HttpSession requireSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED, "登录态已失效，请重新登录");
        }
        return session;
    }
}
