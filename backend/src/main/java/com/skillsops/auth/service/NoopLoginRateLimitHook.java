package com.skillsops.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopLoginRateLimitHook implements LoginRateLimitHook {

    private static final Logger log = LoggerFactory.getLogger(NoopLoginRateLimitHook.class);

    @Override
    public void onLoginFailed(String username) {
        log.info("登录失败限流钩子触发（占位） username={}", username);
    }
}
