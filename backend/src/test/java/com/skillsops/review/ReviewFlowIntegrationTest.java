package com.skillsops.review;

import com.skillsops.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.ResultSet;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@EnabledIf("com.skillsops.testsupport.TestConditions#dockerAvailable")
class ReviewFlowIntegrationTest {

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

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void shouldCompleteSubmitApproveAndRejectFlow() throws Exception {
        String token = "it-" + Instant.now().toEpochMilli();
        String skillNameApprove = "skill-approve-" + token;
        String skillNameReject = "skill-reject-" + token;

        MvcResult csrfResult = mockMvc.perform(get("/v1/auth/csrf-token"))
                .andExpect(status().isOk())
                .andReturn();
        jakarta.servlet.http.Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        String csrfToken = csrfCookie.getValue();

        MockHttpSession userSession = new MockHttpSession();
        userSession.setAttribute(AuthService.SESSION_USER_ID, 101L);
        userSession.setAttribute(AuthService.SESSION_USER_ROLE, "USER");

        createDraft(skillNameApprove, userSession, csrfCookie, csrfToken);
        Long approveSkillId = queryId("SELECT id FROM skill WHERE name = ?", skillNameApprove);
        submitReview(approveSkillId, userSession, csrfCookie, csrfToken);
        Long approveReviewId = queryId("SELECT id FROM review_record WHERE skill_id = ? ORDER BY id DESC LIMIT 1", approveSkillId);

        MockHttpSession adminSession = new MockHttpSession();
        adminSession.setAttribute(AuthService.SESSION_USER_ID, 9001L);
        adminSession.setAttribute(AuthService.SESSION_USER_ROLE, "ADMIN");

        mockMvc.perform(get("/v1/reviews/pending")
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/v1/reviews/{reviewId}/approve", approveReviewId)
                        .session(adminSession)
                        .cookie(csrfCookie)
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String approveStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM skill WHERE id = ?",
                String.class,
                approveSkillId);
        org.junit.jupiter.api.Assertions.assertEquals("published", approveStatus);

        createDraft(skillNameReject, userSession, csrfCookie, csrfToken);
        Long rejectSkillId = queryId("SELECT id FROM skill WHERE name = ?", skillNameReject);
        submitReview(rejectSkillId, userSession, csrfCookie, csrfToken);
        Long rejectReviewId = queryId("SELECT id FROM review_record WHERE skill_id = ? ORDER BY id DESC LIMIT 1", rejectSkillId);

        mockMvc.perform(post("/v1/reviews/{reviewId}/reject", rejectReviewId)
                        .session(adminSession)
                        .cookie(csrfCookie)
                        .header("X-CSRF-Token", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"不满足发布规范，需补充文档后重提"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String rejectSkillStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM skill WHERE id = ?",
                String.class,
                rejectSkillId);
        String reviewStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM review_record WHERE id = ?",
                String.class,
                rejectReviewId);
        org.junit.jupiter.api.Assertions.assertEquals("draft", rejectSkillStatus);
        org.junit.jupiter.api.Assertions.assertEquals("rejected", reviewStatus);

        mockMvc.perform(post("/v1/skills/{skillId}/offline", approveSkillId)
                        .session(adminSession)
                        .cookie(csrfCookie)
                        .header("X-CSRF-Token", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"发现安全风险，先行下架治理"}
                                """))
                .andExpect(status().isOk());

        String offlineStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM skill WHERE id = ?",
                String.class,
                approveSkillId);
        org.junit.jupiter.api.Assertions.assertEquals("offline", offlineStatus);
    }

    private void createDraft(String name, MockHttpSession session, jakarta.servlet.http.Cookie csrfCookie, String csrfToken)
            throws Exception {
        mockMvc.perform(post("/v1/skills")
                        .session(session)
                        .cookie(csrfCookie)
                        .header("X-CSRF-Token", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","description":"integration test","resourceUrl":"https://example.com/%s","categoryId":1}
                                """.formatted(name, name)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private void submitReview(Long skillId, MockHttpSession session, jakarta.servlet.http.Cookie csrfCookie, String csrfToken)
            throws Exception {
        mockMvc.perform(post("/v1/skills/{skillId}/submit-review", skillId)
                        .session(session)
                        .cookie(csrfCookie)
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private Long queryId(String sql, Object arg) {
        return jdbcTemplate.queryForObject(sql, (ResultSet rs, int rowNum) -> rs.getLong(1), arg);
    }
}
