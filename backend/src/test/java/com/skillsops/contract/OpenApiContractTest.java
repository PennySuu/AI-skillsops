package com.skillsops.contract;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skillsops.auth.mapper.UserAccountMapper;
import com.skillsops.category.mapper.CategoryMapper;
import com.skillsops.rating.mapper.RatingMapper;
import com.skillsops.review.mapper.ReviewRecordMapper;
import com.skillsops.skill.mapper.AuditRecordMapper;
import com.skillsops.skill.mapper.InstallRecordMapper;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.mapper.SkillVersionMapper;
import com.skillsops.skill.service.InstallCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * OpenAPI 契约快照（基础断言）：确保关键路径已进入 /v3/api-docs。
 */
@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
        })
@AutoConfigureMockMvc
class OpenApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAccountMapper userAccountMapper;

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

    @MockBean
    private RatingMapper ratingMapper;

    @MockBean
    private CategoryMapper categoryMapper;

    @Test
    void should_exposeExpectedV1Paths_when_openApiGenerated() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/v1/auth/login")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/v1/market/skills")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/v1/admin/ops/dashboard")));
    }
}
