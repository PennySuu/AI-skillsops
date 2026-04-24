package com.skillsops;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 冒烟：加载完整 Spring 上下文并执行 Flyway（需 Docker，见 {@link com.skillsops.testsupport.TestConditions}）。
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@EnabledIf("com.skillsops.testsupport.TestConditions#dockerAvailable")
class SkillsOpsApplicationSmokeTest {

    @Container
    @SuppressWarnings("resource")
    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.0.36").withDatabaseName("skillsops_test");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Test
    void should_loadApplicationContext_when_mysqlAndFlywayReady() {
        // 上下文成功启动即表示 Flyway migrate 已通过
    }
}
