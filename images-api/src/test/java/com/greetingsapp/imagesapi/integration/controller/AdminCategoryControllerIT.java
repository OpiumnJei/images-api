package com.greetingsapp.imagesapi.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.dto.categories.CreateCategoryDTO;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest: Levanta el contexto completo de la aplicación para la prueba.
@SpringBootTest
// @AutoConfigureMockMvc: Configura MockMvc para hacer llamadas HTTP a nuestros endpoints.
@AutoConfigureMockMvc
// @Transactional: Asegura que cada prueba se ejecute en una transacción que se revierte
// al final, manteniendo la base de datos limpia.
@Transactional
public class AdminCategoryControllerIT {

    // MockMvc es la herramienta para simular las peticiones HTTP.
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    // ObjectMapper para convertir objetos Java a JSON.
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCategory_withAdminTokenAndValidData_returns201Created() throws Exception {
        // --- ARRANGE ---
        CreateCategoryDTO newCategoryDTO = new CreateCategoryDTO("Nueva Categoría");
        String newCategoryJson = objectMapper.writeValueAsString(newCategoryDTO);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/admin/categories")
                        // Simula una petición de un usuario autenticado con rol ADMIN.
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategoryJson))
                // Verifica que el código de estado sea 201 Created.
                .andExpect(status().isCreated())
                // Verifica que la respuesta JSON contenga el nombre correcto.
                .andExpect(jsonPath("$.categoryName").value("Nueva Categoría"));
    }

    @Test
    void createCategory_withoutToken_returns401Unauthorized() throws Exception {
        // --- ARRANGE ---
        CreateCategoryDTO newCategoryDTO = new CreateCategoryDTO("Categoría Fantasma");
        String newCategoryJson = objectMapper.writeValueAsString(newCategoryDTO);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/admin/categories")
                        // NO se añade token de autenticación.
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategoryJson))
                // Verifica que la respuesta sea 401 Unauthorized, porque el endpoint está protegido.
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCategory_asClientUser_returns403Forbidden() throws Exception {
        // Arrange
        CreateCategoryDTO newCategoryDTO = new CreateCategoryDTO("Intento no autorizado");
        String newCategoryJson = objectMapper.writeValueAsString(newCategoryDTO);

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                        // Simula una petición con un token JWT válido, pero con el ROL incorrecto.
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCategoryJson))
                // Verifica que la respuesta sea 403 Forbidden (Prohibido).
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_withBlankName_returns400BadRequest() throws Exception {
        // Arrange: Creamos un DTO con el nombre en blanco.
        CreateCategoryDTO invalidDTO = new CreateCategoryDTO("   ");
        String invalidJson = objectMapper.writeValueAsString(invalidDTO);

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                // Verifica que la respuesta sea 400 Bad Request (Petición incorrecta).
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_withExistingName_returns409Conflict() throws Exception {
        // Arrange
        // 1. Primero, insertamos una categoría en la BD de prueba para que ya exista.
        categoryRepository.save(new Category("Nombre Existente", null));

        // 2. Preparamos un DTO con el mismo nombre.
        CreateCategoryDTO duplicateDTO = new CreateCategoryDTO("Nombre Existente");
        String duplicateJson = objectMapper.writeValueAsString(duplicateDTO);

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                // Verifica que la respuesta sea 409 Conflict.
                .andExpect(status().isConflict());
    }
}