package com.greetingsapp.imagesapi.infra.security;

import com.greetingsapp.imagesapi.infra.authentication.TokenService;
import com.greetingsapp.imagesapi.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Este filtro se ejecuta en cada petición para validar el token JWT.
@Component
public class SecurityFilterJWT extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    //FILTRO PERSONALIZADO
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = recoverToken(request);
            if (token != null) {
                String username = tokenService.getSubject(token);
                var user = userRepository.findByUsername(username).orElseThrow();

                var authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (RuntimeException ex) {
            // Si el token es inválido o expiró, limpiamos el contexto para asegurar que no quede basura
            SecurityContextHolder.clearContext();
            // Opcional: Podrías loguear el error aquí: System.out.println("Token error: " + ex.getMessage());
            // No lanzamos la excepción para permitir que Spring Security maneje el rechazo (401/403) más adelante.
        }

        filterChain.doFilter(request, response);
    }
    

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        // Extrae solo el token, sin el prefijo "Bearer ".
        return authHeader.substring(7);
    }
}
