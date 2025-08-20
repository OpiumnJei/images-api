package com.greetingsapp.imagesapi.dto.images;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record UpdateImageDTO(
        @Schema(description = "Nuevo nombre para la imagen.", example = "Atardecer en la playa")
        @NotBlank(message = "Image name is required.")
        String name,
        @Schema(description = "Nuevo texto del saludo.")
        @NotBlank(message = "Image description is required.")
        String description,
        @Schema(description = "Nueva URL de la imagen.")
        @NotBlank
        @URL(message = "Must be a valid URL.")
        String url,
        @Schema(description = "Nuevo ID de la temática para mover la imagen.")
        @NotNull(message = "Theme ID is required.")
        Long themeId
) {
}
