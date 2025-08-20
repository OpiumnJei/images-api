package com.greetingsapp.imagesapi.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greetingsapp.imagesapi.domain.users.Role;
import com.greetingsapp.imagesapi.dto.users.CreateUserDTO;
import com.greetingsapp.imagesapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest: Carga el contexto COMPLETO de tu aplicación.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
// @Transactional: Cada prueba se ejecuta en una transacción que se revierte
// al final, manteniendo la base de datos de prueba limpia.
@Transactional
class AdminUserControllerIT {

    // MockMvc permite simular las peticiones HTTP.
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper para convertir nuestros objetos DTO a JSON.
    @Autowired
    private ObjectMapper objectMapper;

    // Inyectamos el repositorio REAL para verificar el estado de la base de datos.
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_withValidDataAndAdminRole_shouldReturn201Created() throws Exception {
        // --- ARRANGE ---
        CreateUserDTO createDTO = new CreateUserDTO("newUser", "password123", Role.ADMIN);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/admin/users")
                        // Simula una petición con un token JWT de un usuario con rol ADMIN.
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                // Verifica que el estado de la respuesta sea 201 Created.
                .andExpect(status().isCreated())
                // Verifica que el JSON de respuesta contenga los datos esperados.
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Verificación extra en la base de datos (opcional pero muy potente)
        // se verifica si password se encripto correctamente
        var userInDb = userRepository.findByUsername("newUser").orElseThrow();
        assertTrue(passwordEncoder.matches("password123", userInDb.getPassword()));
    }

    @Test
    void createUser_withBlankUsername_shouldReturn400BadRequest() throws Exception {
        // --- ARRANGE ---
        CreateUserDTO invalidDTO = new CreateUserDTO(" ", "password123", Role.ADMIN);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/admin/users")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_asClientUser_shouldReturn403Forbidden() throws Exception {
        // --- ARRANGE ---
        CreateUserDTO createDTO = new CreateUserDTO("newUser", "password123", Role.ADMIN);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/api/admin/users")
                        // Simula una petición con un token JWT de un usuario con rol CLIENT.
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }
}