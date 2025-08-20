package com.greetingsapp.imagesapi.dto.users;

import com.greetingsapp.imagesapi.domain.users.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserDTO(
        @Schema(description = "Nombre de usuario único.", example = "nuevoAdmin", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "User name is required")
        String username,
        @Schema(description = "Contraseña para el nuevo usuario.", example = "supersecret123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        String password,
        @Schema(description = "Rol asignado al usuario.", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Specify a role, please.")
        Role role
) {
}
