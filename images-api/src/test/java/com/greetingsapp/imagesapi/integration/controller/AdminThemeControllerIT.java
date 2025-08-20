package com.greetingsapp.imagesapi.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import com.greetingsapp.imagesapi.dto.themes.CreateThemeDTO;
import com.greetingsapp.imagesapi.dto.themes.UpdateThemeDTO;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
import com.greetingsapp.imagesapi.repository.ThemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminThemeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Category testCategory;
    private Theme testTheme;

    // Se ejecuta antes de cada test para preparar datos comunes en la BD de prueba.
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Categoría de Prueba");
        categoryRepository.save(testCategory);

        testTheme = new Theme();
        testTheme.setName("Temática Existente");
        testTheme.setCategory(testCategory);
        themeRepository.save(testTheme);
    }

    // --- Tests para POST /api/admin/themes ---

    @Test
    void createTheme_withAdminTokenAndValidData_returns201Created() throws Exception {
        CreateThemeDTO newThemeDTO = new CreateThemeDTO("Temática Nueva", testCategory.getId());

        mockMvc.perform(post("/api/admin/themes")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newThemeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.themeName").value("Temática Nueva")); //Json path, se usa para verificar el json enviado como respuesta
    }

    @Test
    void createTheme_withExistingNameInCategory_returns409Conflict() throws Exception {
        // Intenta crear una temática con el mismo nombre que 'testTheme' en la misma categoría.
        CreateThemeDTO duplicateThemeDTO = new CreateThemeDTO("Temática Existente", testCategory.getId());

        mockMvc.perform(post("/api/admin/themes")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateThemeDTO)))
                .andExpect(status().isConflict());
    }

    // --- Tests para PUT /api/admin/themes/{themeId} ---

    @Test
    void updateTheme_withAdminTokenAndValidData_returns200OK() throws Exception {
        UpdateThemeDTO updateDTO = new UpdateThemeDTO("Nombre Actualizado");

        mockMvc.perform(put("/api/admin/themes/" + testTheme.getId())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themeName").value("Nombre Actualizado"));
    }

    @Test
    void updateTheme_withNonExistentId_returns404NotFound() throws Exception {
        UpdateThemeDTO updateDTO = new UpdateThemeDTO("Nombre Fantasma");

        Long nonExistentId = 999L;

        mockMvc.perform(put("/api/admin/themes/" + nonExistentId)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    // --- Tests para DELETE /api/admin/themes/{themeId} ---

    @Test
    void deleteTheme_withAdminTokenAndExistingId_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/themes/" + testTheme.getId())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTheme_asClientUser_returns403Forbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/themes/" + testTheme.getId())
                        // Simula un usuario con rol CLIENT, que no tiene permisos.
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }
}
