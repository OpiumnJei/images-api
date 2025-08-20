package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.dto.themes.CreateThemeDTO;
import com.greetingsapp.imagesapi.dto.themes.ThemeResponseDTO;
import com.greetingsapp.imagesapi.dto.themes.UpdateThemeDTO;
import com.greetingsapp.imagesapi.services.ThemeService;
import io.swagger.v3.oas.annotations.OpenAPI31;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin: Gestion de tematicas")
@SecurityRequirement(name = "bearer-key")//se usa para indicar que un endpoint específico requiere autenticación o autorización
@RestController
@RequestMapping("/api/admin/themes") // Base path para la administración de temáticas
@PreAuthorize("hasRole('ADMIN')")
public class AdminThemeController {

    @Autowired
    private ThemeService themeService;

    // POST /api/admin/themes
    @Operation(summary = "Crea una nueva tematica")
    @PostMapping
    public ResponseEntity<ThemeResponseDTO> createTheme(@RequestBody @Valid CreateThemeDTO createThemeDTO) {
        ThemeResponseDTO newTheme = themeService.createTheme(createThemeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTheme);
    }

    // PUT /api/admin/themes/{themeId}
    @Operation(summary = "Actualiza una tematica")
    @PutMapping("/{themeId}")
    public ResponseEntity<ThemeResponseDTO> updateTheme(@PathVariable Long themeId, @RequestBody @Valid UpdateThemeDTO updateThemeDTO) {
        ThemeResponseDTO updatedTheme = themeService.updateTheme(themeId, updateThemeDTO);
        return ResponseEntity.ok(updatedTheme);
    }

    // DELETE /api/admin/themes/{themeId}
    @Operation(summary = "Elimina una tematica")
    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long themeId) {
        themeService.deleteTheme(themeId);
        return ResponseEntity.noContent().build();
    }
}