package com.skillsops.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsops.auth.service.AuthService;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.skill.service.InstallCommandService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@EnabledIf("com.skillsops.testsupport.TestConditions#dockerAvailable")
class MarketInstallIntegrationTest {

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

    @Autowired
    private InstallCommandService installCommandService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldHideOfflineSkillFromMarket() throws Exception {
        String token = "market-" + Instant.now().toEpochMilli();
        String skillName = "skill-offline-" + token;

        CsrfContext csrf = fetchCsrf();
        MockHttpSession authorSession = userSession(101L, "USER");
        MockHttpSession adminSession = userSession(9001L, "ADMIN");

        Long skillId = createPublishedSkill(skillName, authorSession, adminSession, csrf);

        mockMvc.perform(get("/v1/market/skills")
                        .param("page", "0")
                        .param("size", "20")
                        .param("q", skillName))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(skillName)));

        offlineSkill(skillId, adminSession, csrf);

        mockMvc.perform(get("/v1/market/skills")
                        .param("page", "0")
                        .param("size", "20")
                        .param("q", skillName))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(skillName))));

        mockMvc.perform(get("/v1/market/skills/{skillId}", skillId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldRejectRepeatedOrExpiredInstallCommandConsumption() throws Exception {
        String token = "install-" + Instant.now().toEpochMilli();
        String skillName = "skill-install-" + token;

        CsrfContext csrf = fetchCsrf();
        MockHttpSession authorSession = userSession(202L, "USER");
        MockHttpSession adminSession = userSession(9001L, "ADMIN");
        Long skillId = createPublishedSkill(skillName, authorSession, adminSession, csrf);

        MvcResult commandResult = mockMvc.perform(post("/v1/skills/{skillId}/install-command", skillId)
                        .session(authorSession)
                        .cookie(csrf.cookie())
                        .header("X-CSRF-Token", csrf.token())
                        .header("Idempotency-Key", "idem-" + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(commandResult.getResponse().getContentAsString());
        String command = jsonNode.path("data").path("command").asText();
        String installToken = command.substring(command.lastIndexOf('/') + 1);

        installCommandService.consumeInstallToken(installToken, 202L);

        BusinessException repeated = assertThrows(BusinessException.class,
                () -> installCommandService.consumeInstallToken(installToken, 202L));
        assertEquals(ErrorCode.OPERATION_FAILED, repeated.getErrorCode());

        BusinessException expired = assertThrows(BusinessException.class,
                () -> installCommandService.consumeInstallToken("missing-token-" + token, 202L));
        assertEquals(ErrorCode.OPERATION_FAILED, expired.getErrorCode());
    }

    @Test
    void shouldRejectRatingWhenUserNotInstalled() throws Exception {
        String token = "rating-uninstalled-" + Instant.now().toEpochMilli();
        String skillName = "skill-rating-uninstalled-" + token;
        CsrfContext csrf = fetchCsrf();
        MockHttpSession authorSession = userSession(302L, "USER");
        MockHttpSession adminSession = userSession(9001L, "ADMIN");
        Long skillId = createPublishedSkill(skillName, authorSession, adminSession, csrf);

        mockMvc.perform(put("/v1/skills/{skillId}/ratings", skillId)
                        .session(authorSession)
                        .cookie(csrf.cookie())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":5,"comment":"great"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("RATING_REQUIRES_INSTALL"));
    }

    @Test
    void shouldUpdateRatingByUpsertAndReflectInDetail() throws Exception {
        String token = "rating-upsert-" + Instant.now().toEpochMilli();
        String skillName = "skill-rating-upsert-" + token;
        CsrfContext csrf = fetchCsrf();
        MockHttpSession authorSession = userSession(402L, "USER");
        MockHttpSession adminSession = userSession(9001L, "ADMIN");
        Long skillId = createPublishedSkill(skillName, authorSession, adminSession, csrf);

        jdbcTemplate.update(
                "INSERT INTO install_record (user_id, skill_id, installed_version) VALUES (?, ?, ?)",
                402L,
                skillId,
                "1.0.0");

        mockMvc.perform(put("/v1/skills/{skillId}/ratings", skillId)
                        .session(authorSession)
                        .cookie(csrf.cookie())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":4,"comment":"good"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/v1/skills/{skillId}/ratings", skillId)
                        .session(authorSession)
                        .cookie(csrf.cookie())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":5,"comment":"excellent"}
                                """))
                .andExpect(status().isOk());

        Integer score = jdbcTemplate.queryForObject(
                "SELECT score FROM rating WHERE user_id = ? AND skill_id = ?",
                Integer.class,
                402L,
                skillId);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM rating WHERE user_id = ? AND skill_id = ?",
                Long.class,
                402L,
                skillId);
        assertEquals(5, score);
        assertEquals(1L, count);

        mockMvc.perform(get("/v1/market/skills/{skillId}", skillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avgRating").value(5.0))
                .andExpect(jsonPath("$.data.ratingCount").value(1));
    }

    private Long createPublishedSkill(
            String name,
            MockHttpSession authorSession,
            MockHttpSession adminSession,
            CsrfContext csrf) throws Exception {
        mockMvc.perform(post("/v1/skills")
                        .session(authorSession)
                        .cookie(csrf.cookie())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","description":"market test","resourceUrl":"https://example.com/%s","categoryId":1}
                                """.formatted(name, name)))
                .andExpect(status().isOk());

        Long skillId = queryId("SELECT id FROM skill WHERE name = ?", name);

        mockMvc.perform(post("/v1/skills/{skillId}/submit-review", skillId)
                        .session(authorSession)
                        .cookie(csrf.cookie())
                        .header("X-CSRF-Token", csrf.token()))
                .andExpect(status().isOk());

        Long reviewId = queryId("SELECT id FROM review_record WHERE skill_id = ? ORDER BY id DESC LIMIT 1", skillId);
        mockMvc.perform(post("/v1/reviews/{reviewId}/approve", reviewId)
                        .session(adminSession)
                        .cookie(csrf.cookie())
                        .header("X-CSRF-Token", csrf.token()))
                .andExpect(status().isOk());
        return skillId;
    }

    private void offlineSkill(Long skillId, MockHttpSession adminSession, CsrfContext csrf) throws Exception {
        mockMvc.perform(post("/v1/skills/{skillId}/offline", skillId)
                        .session(adminSession)
                        .cookie(csrf.cookie())
                        .header("X-CSRF-Token", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"发现风险问题，执行下架处理"}
                                """))
                .andExpect(status().isOk());
    }

    private CsrfContext fetchCsrf() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/auth/csrf-token"))
                .andExpect(status().isOk())
                .andReturn();
        jakarta.servlet.http.Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        return new CsrfContext(cookie, cookie.getValue());
    }

    private MockHttpSession userSession(Long userId, String role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthService.SESSION_USER_ID, userId);
        session.setAttribute(AuthService.SESSION_USER_ROLE, role);
        return session;
    }

    private Long queryId(String sql, Object arg) {
        return jdbcTemplate.queryForObject(sql, (ResultSet rs, int rowNum) -> rs.getLong(1), arg);
    }

    private record CsrfContext(jakarta.servlet.http.Cookie cookie, String token) {
    }
}
