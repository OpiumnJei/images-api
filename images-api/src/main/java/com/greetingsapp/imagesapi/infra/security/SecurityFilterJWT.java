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

        String token = recoverToken(request);

        if (token != null) {
            // Si hay un token, obtenemos el username y cargamos los datos del usuario.
            String username = tokenService.getSubject(token);
            var user = userRepository.findByUsername(username).orElseThrow();

            // Creamos un objeto de autenticación y lo guardamos en el contexto de seguridad.
            var authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continuamos con el siguiente filtro en la cadena.
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
