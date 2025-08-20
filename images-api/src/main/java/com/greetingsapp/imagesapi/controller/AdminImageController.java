package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.dto.images.CreateImageDTO;
import com.greetingsapp.imagesapi.dto.images.ImageResponseDTO;
import com.greetingsapp.imagesapi.dto.images.UpdateImageDTO;
import com.greetingsapp.imagesapi.services.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin: Gestion de imagenes", description = "Controlador encargado de la gestion de las imagenes.")
@SecurityRequirement(name = "bearer-key")//se usa para indicar que un endpoint específico requiere autenticación o autorización
@RestController
@RequestMapping("/api/admin/images")
@PreAuthorize("hasRole('ADMIN')") // Protege todos los endpoints de este controlador
public class AdminImageController {

    @Autowired
    private ImageService imageService;

    // --- Endpoint para crear una nueva imagen ---
    // POST /api/admin/images
    @Operation(summary = "Crea una nueva imagen")
    @PostMapping
    public ResponseEntity<ImageResponseDTO> createImage(@RequestBody @Valid CreateImageDTO createImageDTO) {
        ImageResponseDTO newImage = imageService.createImage(createImageDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newImage);
    }

    // --- Endpoint para actualizar una imagen ---
    // PUT /api/admin/images/{imageId}
    @Operation(summary = "Actualiza una nueva imagen")
    @PutMapping("/{imageId}")
    public ResponseEntity<ImageResponseDTO> updateImage(
            @PathVariable Long imageId,
            @RequestBody @Valid UpdateImageDTO updateImageDTO) {

        ImageResponseDTO updatedImage = imageService.updateImage(imageId, updateImageDTO);
        return ResponseEntity.ok(updatedImage);
    }

    // --- Endpoint para eliminar una imagen ---
    // DELETE /api/admin/images/{imageId}
    @Operation(summary = "Elimina una nueva imagen")
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }
}