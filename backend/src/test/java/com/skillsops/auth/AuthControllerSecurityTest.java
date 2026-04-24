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
}
