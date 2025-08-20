package com.greetingsapp.imagesapi.dto.themes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Para crear una temática, necesitas su nombre y el ID de la categoría a la que pertenece.
public record CreateThemeDTO(
        @Schema(description = "Nombre de la nueva temática (debe ser único dentro de su categoría).", example = "Flores y Pasteles", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Theme name is required.")
        String name,

        @Schema(description = "ID de la categoría a la que pertenece esta temática.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Category ID is required.")
        Long categoryId
) {}
