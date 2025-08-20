package com.greetingsapp.imagesapi.infra.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Se marca como @Component para que pueda ser inyectado en SecurityConfig.
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    //Metodo que se ejecuta cuando un usuario no autenticado hace una petición protegida
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Creamos nuestro DTO de error personalizado.
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("UNAUTHORIZED", "A valid token is required to access this resource.");

        // Configuramos la respuesta HTTP.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Código de estado 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); //indicamos que el contenido enviado debe interpretarse como un json

        // Usamos ObjectMapper para escribir nuestro DTO como un JSON en la respuesta.
        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}