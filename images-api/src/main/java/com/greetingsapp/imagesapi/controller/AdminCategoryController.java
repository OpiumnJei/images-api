package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.dto.categories.CategoryResponseDTO;
import com.greetingsapp.imagesapi.dto.categories.CreateCategoryDTO;
import com.greetingsapp.imagesapi.dto.categories.UpdateCategoryDTO;
import com.greetingsapp.imagesapi.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin: Gestion de categorias", description = "Controlador encargado de la gestion de las categorias.")
@SecurityRequirement(name = "bearer-key")//se usa para indicar que un endpoint específico requiere autenticación o autorización
@RestController
@RequestMapping("/api/admin/categories")
// Solo usuarios con el rol 'ADMIN' pueden acceder a este controlador.
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Crea una nueva categoria")
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        // Aquí llamas a un nuevo metodo en tu CategoryService para crear la categoría.
        CategoryResponseDTO newCategory = categoryService.createCategory(createCategoryDTO);
        // Devuelves la nueva categoría con un estado 201 Created.
        return ResponseEntity.status(201).body(newCategory);
    }

    // --- Endpoint para actualizar una categoría ---
    @Operation(summary = "Actualiza una categoria")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody @Valid UpdateCategoryDTO updateCategoryDTO) {

        CategoryResponseDTO updatedCategory = categoryService.updateCategory(categoryId, updateCategoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    //--- Endpoint para eliminar una categoría ---
    @Operation(summary = "Elimina una categoria")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);

        // La respuesta estándar para un DELETE exitoso es 204 No Content.
        // Esto le indica al cliente que la operación fue exitosa y que no hay
        // contenido que devolver en el cuerpo de la respuesta.
        return ResponseEntity.noContent().build();
    }
}

