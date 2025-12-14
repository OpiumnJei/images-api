package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.dto.images.ImageResponseDTO;
import com.greetingsapp.imagesapi.services.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public: Imagenes", description = "Endpoints públicos para consultar todas las imagenes.")
@RestController
@RequestMapping("/api/images") //base path
public class ImageController {


    @Autowired
    private ImageService imageService;

    @Operation(summary = "Obtiene todas las imagenes paginadas.")
    @GetMapping
    public ResponseEntity<Page<ImageResponseDTO>> getAllImages(
            @PageableDefault(size = 20, // parametros por defecto para la paginacion
                    sort = "created",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(imageService.getAllImages(pageable));
    }


}
