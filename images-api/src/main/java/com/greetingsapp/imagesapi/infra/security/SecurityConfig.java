package com.greetingsapp.imagesapi.infra.security;

import com.greetingsapp.imagesapi.infra.errors.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Clase principal de configuración de seguridad.
 * Configura JWT, CORS y reglas de autorización.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilterJWT securityFilterJwt;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    /**
     * Dominios permitidos para CORS.
     * Se lee de la variable de entorno CORS_ALLOWED_ORIGINS.
     * Por defecto permite todos (*) para desarrollo.
     * <p>
     * NOTA: CORS solo aplica para navegadores web (React, Angular, etc.)
     * Las apps móviles nativas (Android/Kotlin, iOS/Swift) NO usan CORS.
     * Si tu único cliente es una app móvil, esta configuración no te afecta.
     */
    @Value("${cors.allowed.origins:*}")
    private String allowedOrigins;

    /**
     * Configura CORS (Cross-Origin Resource Sharing).
     * <p>
     * ¿Qué es CORS?
     * Es un mecanismo de seguridad del navegador que bloquea peticiones
     * desde un dominio diferente al del servidor (ej: tu app móvil/web
     * en https://miapp.com llamando a tu API en https://api.miapp.com).
     * <p>
     * Sin esta configuración, los navegadores bloquearían las peticiones
     * de tu frontend a esta API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Dominios permitidos (ej: "https://miapp.com,https://admin.miapp.com")
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers permitidos (Authorization es crucial para JWT)
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));

        // Permite enviar cookies/credenciales
        configuration.setAllowCredentials(true);

        // Cache de preflight requests (1 hora)
        // Evita que el navegador haga OPTIONS en cada request
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desactivamos CSRF porque usamos JWT (stateless)
                .csrf(csrf -> csrf.disable())
                // Habilitamos CORS con nuestra configuración
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configuramos la gestión de sesiones como STATELESS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Definimos las reglas de autorización para los endpoints
                .authorizeHttpRequests(authorize -> authorize
                        // El endpoint de login es público
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Swagger UI público
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Actuator health público para monitoreo
                        .requestMatchers("/actuator/health/**").permitAll()
                        // Admin requiere rol ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Todos los GET son públicos
                        .requestMatchers(HttpMethod.GET).permitAll()
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                // Manejador de errores 401
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                // Filtro JWT antes del filtro de autenticación de Spring
                .addFilterBefore(securityFilterJwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}