package com.skillsops.auth.service;

import com.skillsops.auth.domain.UserAccount;
import com.skillsops.auth.dto.AuthLoginRequest;
import com.skillsops.auth.dto.AuthProfileResponse;
import com.skillsops.auth.dto.AuthRegisterRequest;
import com.skillsops.auth.mapper.UserAccountMapper;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    public static final String SESSION_USER_ID = "SESSION_USER_ID";
    public static final String SESSION_USER_ROLE = "SESSION_USER_ROLE";

    private static final String DEFAULT_ROLE = "USER";
    private static final long SESSION_EXPIRE_SECONDS = 30 * 60;

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimitHook loginRateLimitHook;

    public AuthService(
            UserAccountMapper userAccountMapper,
            PasswordEncoder passwordEncoder,
            LoginRateLimitHook loginRateLimitHook) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginRateLimitHook = loginRateLimitHook;
    }

    @Transactional
    public AuthProfileResponse register(AuthRegisterRequest request, HttpServletRequest httpServletRequest) {
        UserAccount existing = userAccountMapper.findByUsername(request.username());
        if (existing != null) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "用户名已存在");
        }

        String hash = passwordEncoder.encode(request.password());
        userAccountMapper.insert(request.username(), hash, DEFAULT_ROLE);
        UserAccount created = userAccountMapper.findByUsername(request.username());
        if (created == null) {
            throw new BusinessException(ErrorCode.SYSTEM_INTERNAL_ERROR, "注册失败，请稍后重试");
        }

        return buildSession(created, httpServletRequest);
    }

    public AuthProfileResponse login(AuthLoginRequest request, HttpServletRequest httpServletRequest) {
        UserAccount account = userAccountMapper.findByUsername(request.username());
        if (account == null || !passwordEncoder.matches(request.password(), account.passwordHash())) {
            loginRateLimitHook.onLoginFailed(request.username());
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, "账号或密码错误");
        }
        return buildSession(account, httpServletRequest);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private AuthProfileResponse buildSession(UserAccount account, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_ID, account.id());
        session.setAttribute(SESSION_USER_ROLE, account.role());
        session.setMaxInactiveInterval((int) SESSION_EXPIRE_SECONDS);
        return new AuthProfileResponse(account.id(), account.username(), account.role(), SESSION_EXPIRE_SECONDS);
    }
}
