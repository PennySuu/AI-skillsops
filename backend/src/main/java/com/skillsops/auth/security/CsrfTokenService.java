package com.skillsops.auth.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CsrfTokenService {

    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String CSRF_HEADER_NAME = "X-CSRF-Token";

    public String issueToken(HttpServletResponse response) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Cookie cookie = new Cookie(CSRF_COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        response.addCookie(cookie);
        return token;
    }

    public String readCookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> CSRF_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
