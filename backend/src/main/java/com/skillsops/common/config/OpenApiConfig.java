package com.skillsops.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** SpringDoc OpenAPI 全局信息。 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI skillsOpsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SkillsOps API")
                .version("v1")
                .description("SkillsOps V1 接口定义（任务 2.1 壳层）")
                .contact(new Contact().name("SkillsOps Team")));
    }
}
