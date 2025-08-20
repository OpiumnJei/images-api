package com.greetingsapp.imagesapi.dto.authentication;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

//@NotBlank etiqueta de validacion
public record LoginDTO(
        @Schema(description = "Nombre de usuario para el login.", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Contraseña del usuario.", example = "admin_password_123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        String password
) {}