package com.greetingsapp.imagesapi.infra.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearer-key";

        return new OpenAPI()
                // 1. Abre la configuración de componentes
                .components(new Components()
                        // 2. Añade el esquema de seguridad AL OBJETO COMPONENTS
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                ) // 3. Cierra la configuración de componentes

                // 4. Añade el requisito de seguridad AL OBJETO OPENAPI
                .addSecurityItem(
                        new SecurityRequirement().addList(securitySchemeName) // Corregido: sin "name:"
                );
    }
}