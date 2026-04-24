package com.skillsops.auth.service;

/**
 * 登录失败限流钩子（后续可替换为 Sentinel/Resilience4j 实现）。
 */
public interface LoginRateLimitHook {

    void onLoginFailed(String username);
}
