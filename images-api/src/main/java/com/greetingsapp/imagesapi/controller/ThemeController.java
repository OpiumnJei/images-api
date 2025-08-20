package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.dto.images.ImageResponseDTO;
import com.greetingsapp.imagesapi.services.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public: Temáticas", description = "Endpoints públicos para consultar temáticas y sus recursos asociados.")
@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    @Autowired
    private ImageService imageService;

    @Operation(summary = "Obtiene todas las imagenes de una tematica.")
    //Dentro de la colección de temáticas, para la temática con un themeId específico, dame sus imágenes.
    //metodo que obtiene todas las imagenes paginadas que pertenecen a una tematica especifica
    @GetMapping("/{themeId}/images")
    public ResponseEntity<Page<ImageResponseDTO>> getImagesByTheme(@PathVariable Long themeId, Pageable pageable){
        return ResponseEntity.ok(imageService.getImages(themeId, pageable));

        //Hace lo mismo que la linea de arriba:
        //Page<ImageResponseDTO> images = imageService.getImages(themeId, pageable);
        //return ResponseEntity.ok(images);
    }
}
