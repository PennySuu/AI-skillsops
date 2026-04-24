package com.skillsops.common.config;

import com.skillsops.auth.security.CsrfProtectionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 工程骨架阶段的 Security 占位：放行所有请求，避免默认表单登录阻碍健康检查。
 * <p>
 * 认证与 CSRF 硬门禁在任务 3.x 按设计收紧。
 */
@Configuration
@EnableWebSecurity
public class SecurityBootstrapConfig {

    private final CsrfProtectionFilter csrfProtectionFilter;

    public SecurityBootstrapConfig(CsrfProtectionFilter csrfProtectionFilter) {
        this.csrfProtectionFilter = csrfProtectionFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .addFilterBefore(csrfProtectionFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
