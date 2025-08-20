package com.greetingsapp.imagesapi.dto.themes;

import io.swagger.v3.oas.annotations.media.Schema;

public record ThemeResponseDTO(
        @Schema(description = "ID único de la temática.")
        Long themeId,

        @Schema(description = "Nombre de la temática.")
        String themeName
) {
}
