package com.greetingsapp.imagesapi.dto.categories;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateCategoryDTO(
        @Schema(description = "Nombre único para la nueva categoría.", example = "Saludos de Cumpleaños", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "The category name is required.")
        String name
) {
}
