package com.greetingsapp.imagesapi.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDTO(
        @Schema(description = "ID único del usuario.")
        Long id,

        @Schema(description = "Nombre de usuario.")
        String username,

        @Schema(description = "Rol del usuario.")
        String role
) {
}
