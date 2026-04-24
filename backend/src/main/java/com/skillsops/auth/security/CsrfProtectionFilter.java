package com.skillsops.auth.security;

import com.skillsops.common.api.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CsrfProtectionFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final CsrfTokenService csrfTokenService;
    private final ObjectMapper objectMapper;

    public CsrfProtectionFilter(CsrfTokenService csrfTokenService, ObjectMapper objectMapper) {
        this.csrfTokenService = csrfTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (SAFE_METHODS.contains(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String cookieToken = csrfTokenService.readCookieToken(request);
        String headerToken = request.getHeader(CsrfTokenService.CSRF_HEADER_NAME);
        if (cookieToken == null || headerToken == null || !cookieToken.equals(headerToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorEnvelope(false, ErrorCode.AUTH_CSRF_INVALID.getCode(), "CSRF 校验失败", null)));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private record ErrorEnvelope(boolean success, String code, String message, Object data) {
    }
}
