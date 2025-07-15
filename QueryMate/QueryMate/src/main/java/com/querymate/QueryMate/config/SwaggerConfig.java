package com.querymate.QueryMate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customSwaggerConfig(){
        return new OpenAPI().info(
                        new Info().title("QueryMate")
                                .description("Chat with your Database in your Language.")
                )
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("local"),
                        new Server().url("http://localhost:9090").description("live"))
                )
                .tags(Arrays.asList(
                        new Tag().name("Auth APIs"),
                        new Tag().name("User APIs"),
                        new Tag().name("Project APIs"),
                        new Tag().name("Chat APIs")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                ));
    }
}
