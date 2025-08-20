package com.greetingsapp.imagesapi.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.images.Image;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import com.greetingsapp.imagesapi.dto.images.CreateImageDTO;
import com.greetingsapp.imagesapi.dto.images.UpdateImageDTO;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
import com.greetingsapp.imagesapi.repository.ImageRepository;
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
public class AdminImageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ImageRepository imageRepository;

    private Category testCategory;
    private Theme testTheme;
    private Image testImage;

    // Se ejecuta antes de cada test para preparar datos comunes en la BD de prueba.
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Categoría de Prueba");
        categoryRepository.save(testCategory);

        testTheme = new Theme();
        testTheme.setName("Temática de Prueba");
        testTheme.setCategory(testCategory);
        themeRepository.save(testTheme);

        testImage = new Image();
        testImage.setName("Imagen Existente");
        testImage.setUrl("http://example.com/image.jpg");
        testImage.setTheme(testTheme);
        imageRepository.save(testImage);
    }

    // --- Tests para POST /api/admin/images ---

    @Test
    void createImage_withAdminTokenAndValidData_returns201Created() throws Exception {
        CreateImageDTO newImageDTO = new CreateImageDTO("Imagen Nueva",
                "Descripción",
                "http://example.com/new.jpg",
                testTheme.getId());

        mockMvc.perform(post("/api/admin/images")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newImageDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageName").value("Imagen Nueva"));
    }

    @Test
    void createImage_forNonExistentTheme_returns404NotFound() throws Exception {
        CreateImageDTO newImageDTO = new CreateImageDTO("Imagen Fantasma", "Descripción", "http://example.com/ghost.jpg", 999L);

        mockMvc.perform(post("/api/admin/images")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newImageDTO)))
                .andExpect(status().isNotFound());
    }

    // --- Tests para PUT /api/admin/images/{imageId} ---

    @Test
    void updateImage_withAdminTokenAndValidData_returns200OK() throws Exception {
        UpdateImageDTO updateDTO = new UpdateImageDTO("Nombre Actualizado", "Desc Actualizada", "http://new.url", testTheme.getId());

        mockMvc.perform(put("/api/admin/images/" + testImage.getId())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageName").value("Nombre Actualizado"));
    }

    @Test
    void updateImage_withNonExistentId_returns404NotFound() throws Exception {
        UpdateImageDTO updateDTO = new UpdateImageDTO("Nombre Fantasma", "Desc", "http://url.com", testTheme.getId());

        Long nonExistentId = 999L;

        mockMvc.perform(put("/api/admin/images/" + nonExistentId)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    // --- Tests para DELETE /api/admin/images/{imageId} ---

    @Test
    void deleteImage_withAdminTokenAndExistingId_returns204NoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/images/" + testImage.getId())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteImage_asClientUser_returns403Forbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/images/" + testImage.getId())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }
}