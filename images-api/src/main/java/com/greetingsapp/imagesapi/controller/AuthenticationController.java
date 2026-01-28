package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.domain.users.User;
import com.greetingsapp.imagesapi.dto.authentication.LoginDTO;
import com.greetingsapp.imagesapi.dto.authentication.TokenResponseDTO;
import com.greetingsapp.imagesapi.infra.authentication.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public: Login",
        description = "Se genera bearer token para la autorizacion en el resto de endpoints.")//anotacion de springdoc
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Operation(summary = "Login", description = "El usuario introduce sus credenciales, y si esta registrado, se genera un JWT.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        // Crea el objeto de autenticación con las credenciales del usuario.
        var authToken = new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password());

        // Spring Security procesa la autenticación. Si falla, lanza una excepción.
        var authentication = authenticationManager.authenticate(authToken);

        // Si la autenticación es exitosa, genera un token JWT.
        var token = tokenService.generateToken((User) authentication.getPrincipal());

        // Devuelve el token en la respuesta.
        return ResponseEntity.ok(new TokenResponseDTO(token));
    }
}
