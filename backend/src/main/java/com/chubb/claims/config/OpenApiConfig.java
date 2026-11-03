package com.chubb.claims.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI chubbClaimsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chubb APAC Claims Management Platform API")
                        .description("Production-grade RESTful API for Motor and Property Claims processing across 6 APAC markets.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Chubb APAC Engineering")
                                .email("engineering@chubb.com")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from /api/v1/auth/login")));
    }
}
