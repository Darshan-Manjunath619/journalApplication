package com.darshan.journalApplication.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customConfig() {
        return new OpenAPI().info(
                new Info().title("Journal Identity API")
                        .version("v1")
                        .description("Versioned API for authentication, profiles, and administration. "
                                + "Validation and error responses use RFC 9457 Problem Detail.")
        ).tags(List.of(
                new Tag().name("Authentication").description("Registration and token lifecycle"),
                new Tag().name("Profile").description("Current-user profile and password")
        )).components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("15-minute JWT access token returned by login or refresh")
                        )
                );
    }

    @Bean
    public GroupedOpenApi versionOneApi() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }

}
