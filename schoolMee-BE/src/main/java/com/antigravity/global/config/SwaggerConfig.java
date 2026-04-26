package com.antigravity.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI schoolMeeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SchoolMee Admin API")
                        .description("B2B2C 앨범 자동 생성 플랫폼 SchoolMee 관리자 API 문서")
                        .version("v1.0.0"));
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }
}
