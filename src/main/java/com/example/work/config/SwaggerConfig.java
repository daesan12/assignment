package com.example.work.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("바로인턴 10기 과제 API 문서")
                        .description("이 문서는 바로인턴 10기 과제의 API 명세서입니다.")
                        .version("1.0.0"));
    }
}
