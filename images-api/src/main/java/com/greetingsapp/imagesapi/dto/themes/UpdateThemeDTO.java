package com.greetingsapp.imagesapi.dto.themes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// Para actualizar, usualmente solo cambias el nombre.
public record UpdateThemeDTO(
        @Schema(description = "Nuevo nombre para la temática.", example = "Pasteles y Velas", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Theme name must not be blank.")
        String name
) {}