package com.greetingsapp.imagesapi.infra.security;

import com.greetingsapp.imagesapi.infra.errors.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// La clase principal de configuración de seguridad.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilterJWT securityFilterJwt;

    // Inyectamos nuestro nuevo manejador de errores 401.
    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desactivamos CSRF porque usamos JWT (stateless). -> sin sesion
                .csrf(csrf -> csrf.disable())
                // Configuramos la gestión de sesiones como STATELESS.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Definimos las reglas de autorización para los endpoints.
                .authorizeHttpRequests(authorize -> authorize
                        // El endpoint de login es público.
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // --- NUEVAS REGLAS PARA SWAGGER ---
                        // Permite el acceso a la UI de Swagger y a la definición de la API
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") //se requiere tener el rol admin para acceder a esta ruta
                        // Todos los endpoints GET son públicos para que cualquiera vea las imágenes.
                        .requestMatchers(HttpMethod.GET).permitAll()
                        // Cualquier otro endpoint (POST, PUT, DELETE) requiere autenticación.
                        .anyRequest().authenticated()
                )
                // Le decimos a Spring Security cómo manejar las excepciones.
                .exceptionHandling(
                        // Usa nuestra clase personalizada para los errores de autenticación (401).
                        exceptions ->
                                exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                // Añadimos nuestro filtro(securityFilterJwt) JWT antes del filtro de autenticación de Spring(UsernamePasswordAuthenticationFilter)
                .addFilterBefore(securityFilterJwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}