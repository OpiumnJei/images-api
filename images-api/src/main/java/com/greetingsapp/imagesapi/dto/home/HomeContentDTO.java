package com.greetingsapp.imagesapi.dto.home;

import com.greetingsapp.imagesapi.dto.images.ImageResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// DTO para representar el contenido de la pantalla de inicio
public record HomeContentDTO(
        @Schema(description = "Tipo de contenido: 'SPECIAL_EVENT' o 'DEFAULT'") // @
        String type,

        @Schema(description = "Título a mostrar en la UI (ej: 'Feliz San Valentín' o 'Lo Último')")
        String title,

        @Schema(description = "Lista de imágenes a mostrar")
        List<ImageResponseDTO> images
) {
}