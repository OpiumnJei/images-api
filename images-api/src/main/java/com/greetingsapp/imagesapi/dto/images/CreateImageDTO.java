package com.greetingsapp.imagesapi.dto.images;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.rmi.MarshalException;

public record CreateImageDTO(
        @Schema(description = "Nombre o título de la imagen.", example = "Amanecer en la playa", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Image name is required.")
        String name,
        @Schema(description = "Texto del saludo o descripción opcional.", example = "¡Que tu día sea radiante!")
        @NotBlank(message = "Image description is required.")
        String description,
        @Schema(description = "URL pública y completa de la imagen.", example = "https://example.com/playa.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @URL(message = "Must be a valid URL.")
        String url,
        @Schema(description = "ID de la temática a la que pertenece la imagen.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Theme ID is required.")
        Long themeId
) {
}
