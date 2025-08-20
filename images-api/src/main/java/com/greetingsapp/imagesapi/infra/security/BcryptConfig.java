package com.greetingsapp.imagesapi.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Configuración para que Spring sepa cómo encriptar y verificar contraseñas.
@Configuration
public class BcryptConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usamos BCrypt, el estándar para encriptación de contraseñas.
        return new BCryptPasswordEncoder();
    }
}