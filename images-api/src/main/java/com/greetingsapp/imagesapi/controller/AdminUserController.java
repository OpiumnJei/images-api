package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.domain.users.User;
import com.greetingsapp.imagesapi.dto.users.CreateUserDTO;
import com.greetingsapp.imagesapi.dto.users.UserResponseDTO;
import com.greetingsapp.imagesapi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin: Gestion de usuarios")
@SecurityRequirement(name = "bearer-key")//se usa para indicar que un endpoint específico requiere autenticación o autorización
@RestController
@RequestMapping("/api/admin/users") // Base path para la administración de temáticas
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Crea un nuevo usuario")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid
                                                      CreateUserDTO createUserDTO) {
        UserResponseDTO newUser = userService.createUser(createUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }
}
