package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import com.greetingsapp.imagesapi.domain.themes.ThemeMapper;
import com.greetingsapp.imagesapi.dto.themes.CreateThemeDTO;
import com.greetingsapp.imagesapi.dto.themes.ThemeResponseDTO;
import com.greetingsapp.imagesapi.dto.themes.UpdateThemeDTO;
import com.greetingsapp.imagesapi.infra.errors.DuplicateResourceException;
import com.greetingsapp.imagesapi.infra.errors.ResourceNotFoundException;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
import com.greetingsapp.imagesapi.repository.ThemeRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class ThemeService {

    private static final Logger log = LoggerFactory.getLogger(ThemeService.class);

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ThemeMapper themeMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    // --- Metodo para crear una nueva temática --- ✍️
    @Transactional
    public ThemeResponseDTO createTheme(CreateThemeDTO createThemeDTO) {
        // 1. Valida que la categoría padre exista.
        Category category = categoryRepository.findById(createThemeDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + createThemeDTO.categoryId()));

        // 2. VALIDACIÓN: Asegura que el nombre de la temática sea único DENTRO de esa categoría.
        if (themeRepository.findByNameAndCategory(createThemeDTO.name(), category).isPresent()) {
            throw new DuplicateResourceException("Theme with name '" + createThemeDTO.name() + "' already exists in this category.");
        }

        // 3. Crea la nueva entidad Theme.
        Theme newTheme = new Theme();
        newTheme.setName(createThemeDTO.name());
        newTheme.setCategory(category); // Asocia la temática a su categoría.

        // 4. Guarda en la base de datos.
        Theme savedTheme = themeRepository.save(newTheme);

        // 5. Devuelve el DTO de respuesta.
        return themeMapper.themeToThemeResponseDTO(savedTheme);
    }

    // --- Metodo para actualizar una temática --- 🔄
    @Transactional
    public ThemeResponseDTO updateTheme(Long themeId, UpdateThemeDTO updateThemeDTO) {
        Theme themeToUpdate = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + themeId));

        themeToUpdate.setName(updateThemeDTO.name());
        Theme updatedTheme = themeRepository.save(themeToUpdate);

        return themeMapper.themeToThemeResponseDTO(updatedTheme);
    }

    // --- Metodo para eliminar una temática --- 🗑️
    @Transactional
    public void deleteTheme(Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new ResourceNotFoundException("Theme not found with id: " + themeId);
        }
        themeRepository.deleteById(themeId);
    }

    //metodo para traer todas las tematicas de una categoria especifica

    /**
     * Obtiene todas las temáticas de una categoría específica.
     * <p>
     * Patrones de resiliencia aplicados:
     * - @RateLimiter: Limita peticiones para proteger el servidor
     * - @CircuitBreaker: Previene fallos en cascada si la BD está caída
     * - @Retry: Reintenta automáticamente ante fallos transitorios
     */
    @Cacheable(value = "themesByCategory")
    @RateLimiter(name = "publicApiRL")
    @CircuitBreaker(name = "databaseCB", fallbackMethod = "getThemesFallback")
    @Retry(name = "databaseRetry")
    public List<ThemeResponseDTO> getThemes(Long categoryId) {

        if (!categoryRepository.existsById(categoryId)) {
            // Si la categoria NO EXISTE, esto SÍ es un error "No Encontrado".
            // Aquí sí lanzas tu excepción personalizada.
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }

        List<Theme> themes = themeRepository.findByCategoryId(categoryId);

        return themeMapper.themeListToThemeResponseDTOList(themes);
    }

    /**
     * Fallback para getThemes: devuelve lista vacía cuando el servicio falla
     */
    private List<ThemeResponseDTO> getThemesFallback(Long categoryId, Exception ex) {
        log.error("Fallback activado en getThemes para categoryId={}. Causa: {}", categoryId, ex.getMessage());
        return Collections.emptyList();
    }
}
