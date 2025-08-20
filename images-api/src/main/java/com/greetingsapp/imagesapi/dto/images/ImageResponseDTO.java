package com.greetingsapp.imagesapi.dto.images;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageResponseDTO(
        @Schema(description = "ID único de la imagen.")
        Long imageId,

        @Schema(description = "Nombre o título de la imagen.")
        String imageName,

        @Schema(description = "Texto del saludo o descripción.")
        String imageDescription,

        @Schema(description = "URL para visualizar la imagen.")
        String imageUrl
) {
}
