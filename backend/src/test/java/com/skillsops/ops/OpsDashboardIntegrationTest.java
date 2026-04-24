package com.skillsops.ops;

import com.skillsops.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@EnabledIf("com.skillsops.testsupport.TestConditions#dockerAvailable")
class OpsDashboardIntegrationTest {

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

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectDashboardWhenDaysTooSmall() throws Exception {
        MockHttpSession adminSession = adminSession();
        mockMvc.perform(get("/v1/admin/ops/dashboard")
                        .session(adminSession)
                        .param("granularity", "day")
                        .param("days", "0"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldRejectDashboardWhenDaysTooLarge() throws Exception {
        MockHttpSession adminSession = adminSession();
        mockMvc.perform(get("/v1/admin/ops/dashboard")
                        .session(adminSession)
                        .param("granularity", "day")
                        .param("days", "366"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldRejectDashboardWhenGranularityInvalid() throws Exception {
        MockHttpSession adminSession = adminSession();
        mockMvc.perform(get("/v1/admin/ops/dashboard")
                        .session(adminSession)
                        .param("granularity", "quarter")
                        .param("days", "7"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldRejectDashboardWhenCallerNotAdmin() throws Exception {
        MockHttpSession userSession = new MockHttpSession();
        userSession.setAttribute(AuthService.SESSION_USER_ID, 1001L);
        userSession.setAttribute(AuthService.SESSION_USER_ROLE, "USER");
        mockMvc.perform(get("/v1/admin/ops/dashboard")
                        .session(userSession)
                        .param("granularity", "day")
                        .param("days", "7"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
    }

    @Test
    void shouldReturnEmptyDashboardWhenNoPublishedOrInstallData() throws Exception {
        mockMvc.perform(get("/v1/admin/ops/dashboard")
                        .session(adminSession())
                        .param("granularity", "day")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.granularity").value("day"))
                .andExpect(jsonPath("$.data.days").value(7))
                .andExpect(jsonPath("$.data.metrics[0].value").value(0))
                .andExpect(jsonPath("$.data.metrics[1].value").value(0))
                .andExpect(jsonPath("$.data.metrics[2].value").value(0))
                .andExpect(jsonPath("$.data.installTrend", hasSize(0)))
                .andExpect(jsonPath("$.data.topSkills", hasSize(0)))
                .andExpect(jsonPath("$.data.activeAuthors", hasSize(0)));
    }

    private static MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.SESSION_USER_ID, 9001L);
        session.setAttribute(AuthService.SESSION_USER_ROLE, "ADMIN");
        return session;
    }
}
