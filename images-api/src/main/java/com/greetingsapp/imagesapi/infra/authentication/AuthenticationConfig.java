package com.greetingsapp.imagesapi.infra.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

// Provee el AuthenticationManager, el componente central de Spring para procesar la autenticación.
@Configuration
public class AuthenticationConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Obtiene y expone el gestor de autenticación de Spring.
        return configuration.getAuthenticationManager();
    }
}