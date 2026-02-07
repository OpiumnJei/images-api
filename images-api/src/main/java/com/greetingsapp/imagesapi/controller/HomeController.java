package com.greetingsapp.imagesapi.controller;

import com.greetingsapp.imagesapi.dto.home.HomeContentDTO;
import com.greetingsapp.imagesapi.services.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public: Home", description = "Endpoint inteligente para la pantalla de inicio.")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @Operation(summary = "Obtiene el contenido dinámico del inicio (Festividad o Novedades)")
    @GetMapping
    public ResponseEntity<HomeContentDTO> getHomeContent() {
        return ResponseEntity.ok(homeService.getHomeContent());
    }
}