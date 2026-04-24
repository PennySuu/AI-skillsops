package com.skillsops.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skillsops.auth.mapper.UserAccountMapper;
import com.skillsops.auth.dto.AuthProfileResponse;
import com.skillsops.auth.service.AuthService;
import com.skillsops.category.mapper.CategoryMapper;
import com.skillsops.ops.mapper.OpsDashboardMapper;
import com.skillsops.rating.mapper.RatingMapper;
import com.skillsops.review.mapper.ReviewRecordMapper;
import com.skillsops.skill.mapper.AuditRecordMapper;
import com.skillsops.skill.mapper.InstallRecordMapper;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.mapper.SkillVersionMapper;
import com.skillsops.skill.service.InstallCommandService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
        })
@AutoConfigureMockMvc
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserAccountMapper userAccountMapper;

    @MockBean
    private CategoryMapper categoryMapper;

    @MockBean
    private OpsDashboardMapper opsDashboardMapper;

    @MockBean
    private RatingMapper ratingMapper;

    @MockBean
    private SkillMapper skillMapper;

    @MockBean
    private SkillVersionMapper skillVersionMapper;

    @MockBean
    private ReviewRecordMapper reviewRecordMapper;

    @MockBean
    private AuditRecordMapper auditRecordMapper;

    @MockBean
    private InstallRecordMapper installRecordMapper;

    @MockBean
    private InstallCommandService installCommandService;

    @Test
    void should_rejectWriteRequest_when_csrfMissing() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo_user","password":"Passw0rd!"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_CSRF_INVALID"));
    }

    @Test
    void should_allowRegister_when_doubleSubmitTokenValid() throws Exception {
        Mockito.when(authService.register(any(), any()))
                .thenReturn(new AuthProfileResponse(1L, "demo_user", "USER", 1800L));

        String csrfToken = mockMvc.perform(get("/v1/auth/csrf-token"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn()
                .getResponse()
                .getCookie("XSRF-TOKEN")
                .getValue();

        mockMvc.perform(post("/v1/auth/register")
                        .cookie(new jakarta.servlet.http.Cookie("XSRF-TOKEN", csrfToken))
                        .header("X-CSRF-Token", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo_user","password":"Passw0rd!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("demo_user"));
    }

    @Test
    void should_allowLoginAndLogout_when_csrfValid() throws Exception {
        Mockito.when(authService.login(any(), any()))
                .thenReturn(new AuthProfileResponse(2L, "demo_user", "USER", 1800L));

        String csrfToken = mockMvc.perform(get("/v1/auth/csrf-token"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("XSRF-TOKEN")
                .getValue();

        mockMvc.perform(post("/v1/auth/login")
                        .cookie(new jakarta.servlet.http.Cookie("XSRF-TOKEN", csrfToken))
                        .header("X-CSRF-Token", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo_user","password":"Passw0rd!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(2));

        mockMvc.perform(post("/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("XSRF-TOKEN", csrfToken))
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
