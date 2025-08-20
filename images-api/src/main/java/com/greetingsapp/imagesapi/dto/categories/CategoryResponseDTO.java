package com.greetingsapp.imagesapi.dto.categories;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryResponseDTO(
        @Schema(description = "ID único de la categoría.")
        Long categoryId,

        @Schema(description = "Nombre de la categoría.")
        String categoryName
) {
}
