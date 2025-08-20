package com.greetingsapp.imagesapi.infra.authentication;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.greetingsapp.imagesapi.domain.users.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// Servicio encargado de la generación y validación de tokens JWT.
@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String apiSecret;

    //Metodo que genera el token
    public String generateToken(User user) {
        try {
            // Usa la clave secreta para crear el algoritmo de firma.
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            return JWT.create()
                    .withIssuer("greetings-api") // Quién emite el token
                    .withSubject(user.getUsername()) // A quién va dirigido (el usuario)
                    .withClaim("id", user.getId()) // Un "claim" o dato extra: el ID del usuario
                    .withClaim("role", user.getRole().name()) // Otro claim: el rol del usuario
                    .withExpiresAt(generateExpirationTime()) // Fecha de expiración
                    .sign(algorithm); // Firma el token
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error generating JWT token", exception);
        }
    }

    public String getSubject(String token) {
        if (token == null) {
            throw new RuntimeException("Token is null");
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            // Verifica que el token sea válido (firma, emisor) y lo decodifica.
            return JWT.require(algorithm)
                    .withIssuer("greetings-api")
                    .build()
                    .verify(token)
                    .getSubject(); // Devuelve el "subject" (el username)
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Invalid or expired JWT token");
        }
    }

    private Instant generateExpirationTime() {
        // El token expirará en 2 horas desde ahora.
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-04:00"));
    }
}