package com.mhconsultingbe.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI mhConsultingOpenApi() {
        return new OpenAPI()
                .info(new Info().title("MH CONSULTING API").version("v1")
                        .description("Public website and administrator API. Call GET /api/auth/csrf before mutating requests; Swagger UI automatically forwards the resulting XSRF-TOKEN cookie.")
                        .contact(new Contact().name("MH CONSULTING").email("info@mhconsulting.vn")))
                .schemaRequirement("sessionCookie", new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE).name("MHCONSULTING_SESSION"));
    }

    @Bean
    GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public API")
                .pathsToMatch("/api/public/**", "/api/auth/**")
                .build();
    }

    @Bean
    GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("Administrator API")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}
