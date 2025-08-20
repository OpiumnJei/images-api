package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.dto.categories.CategoryResponseDTO;
import com.greetingsapp.imagesapi.dto.themes.ThemeResponseDTO;
import com.greetingsapp.imagesapi.services.CategoryService;
import com.greetingsapp.imagesapi.services.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public: Categorias", description = "Endpoints públicos para consultar categorias y sus recursos asociados.")
@RestController
@RequestMapping("/api/categories") //base path
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ThemeService themeService;

    @Operation(summary = "Obtiene todas las categorias")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Obtiene las tematicas de una categoria")
    @GetMapping("/{categoryId}/themes")
    public ResponseEntity<List<ThemeResponseDTO>> getThemeByCategory(@PathVariable Long categoryId){
        return ResponseEntity.ok(themeService.getThemes(categoryId));
    }
}
