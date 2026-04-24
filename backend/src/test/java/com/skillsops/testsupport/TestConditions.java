package com.skillsops.testsupport;

import org.testcontainers.DockerClientFactory;

/** JUnit {@code @EnabledIf} 条件：本机可用 Docker 时启用 Testcontainers 集成测试。 */
public final class TestConditions {

    private TestConditions() {}

    public static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }
}
