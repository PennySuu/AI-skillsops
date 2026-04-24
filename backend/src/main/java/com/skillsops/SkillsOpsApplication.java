package com.skillsops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** SkillsOps 后端入口。 */
@SpringBootApplication
@MapperScan({
        "com.skillsops.auth.mapper",
        "com.skillsops.common.mapper",
        "com.skillsops.skill.mapper",
        "com.skillsops.review.mapper",
        "com.skillsops.rating.mapper",
        "com.skillsops.category.mapper"
})
public class SkillsOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillsOpsApplication.class, args);
    }
}
