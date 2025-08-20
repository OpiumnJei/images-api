package com.greetingsapp.imagesapi.dto.categories;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryDTO(
        @Schema(description = "Nuevo nombre para la categoría.", example = "Feliz Cumpleaños", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Category name must not be blank.")//traduccion: El nombre de la categoria no debe estar en blanco
        String name
) {
}
