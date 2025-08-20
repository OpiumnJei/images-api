package com.greetingsapp.imagesapi.infra.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing // <-- ¡ACTIVAR AUDITORÍA!
public class PersistenceConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {

//        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
//                .map(Authentication::getName);

        // La lógica para obtener el auditor se queda igual
        return () -> Optional.of("OpiumJei"); // Placeholder para desarrollo
    }
}
