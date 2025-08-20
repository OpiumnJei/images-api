package com.greetingsapp.imagesapi.dto.authentication;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponseDTO(
        @Schema(description = "Token JWT generado tras un login exitoso.")
        String token
) {
}
