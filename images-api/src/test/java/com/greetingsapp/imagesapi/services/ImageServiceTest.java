package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.images.Image;
import com.greetingsapp.imagesapi.domain.images.ImageMapper;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import com.greetingsapp.imagesapi.dto.images.CreateImageDTO;
import com.greetingsapp.imagesapi.dto.images.ImageResponseDTO;
import com.greetingsapp.imagesapi.infra.errors.ResourceNotFoundException;
import com.greetingsapp.imagesapi.repository.ImageRepository;
import com.greetingsapp.imagesapi.repository.ThemeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ImageMapper imageMapper;

    @InjectMocks
    private ImageService imageService;

    @Test
    void shouldReturnImagePage_whenThemeExists() {
        // Arrange
        Long themeId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Image imageEntity = new Image(); // Entidad

        Page<Image> imagePage = new PageImpl<>(List.of(imageEntity));

        ImageResponseDTO imageDTO = new ImageResponseDTO(100L, "Café y sol", "Un saludo...", "http://...");

        when(themeRepository.existsById(themeId)).thenReturn(true);
        when(imageRepository.findByThemeId(themeId, pageable)).thenReturn(imagePage);

        // Usamos thenAnswer para simular la operación .map() de la página
        when(imageMapper.imageToImageResponseDTO(any(Image.class))).thenReturn(imageDTO);

        // Act
        Page<ImageResponseDTO> result = imageService.getImages(themeId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Café y sol", result.getContent().get(0).imageName());
    }

    @Test
    void shouldReturnEmptyPage_whenThemeHasNoImages() {
        // Arrange
        Long themeId = 2L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Image> emptyImagePage = new PageImpl<>(Collections.emptyList());

        when(themeRepository.existsById(themeId)).thenReturn(true);
        when(imageRepository.findByThemeId(themeId, pageable)).thenReturn(emptyImagePage);

        // Act
        Page<ImageResponseDTO> result = imageService.getImages(themeId, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void shouldThrowResourceNotFound_whenThemeDoesNotExist() {
        // Arrange
        Long themeId = 99L;
        Pageable pageable = PageRequest.of(0, 10);

        when(themeRepository.existsById(themeId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                imageService.getImages(themeId, pageable)
        );

        verify(imageRepository, never()).findByThemeId(any(), any());
    }

    @Test
    void shouldCreateImage_whenDataIsValid() {
// --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de entrada.
        CreateImageDTO createDTO = new CreateImageDTO(
                "Imagen de café",
                "Un saludo con café.",
                "http://example.com/cafe.jpg",
                1L
        );

        // 2. Simula la temática padre que existe en la BD.
        Theme parentTheme = new Theme();
        parentTheme.setId(1L);

        // 3. Simula la entidad Image después de ser guardada.
        Image savedImage = new Image();
        savedImage.setId(100L); // ID simulado
        savedImage.setName(createDTO.name());
        savedImage.setTheme(parentTheme);

        // 4. El DTO de respuesta esperado.
        ImageResponseDTO expectedResponseDTO = new ImageResponseDTO(100L, "Imagen de café", "Un saludo con café.", "http://example.com/cafe.jpg");

        // 5. Configuramos el comportamiento de los mocks.
        // "Cuando se busque la temática padre por ID, encuéntrala".
        when(themeRepository.findById(createDTO.themeId())).thenReturn(Optional.of(parentTheme));

        // "Cuando se guarde la nueva imagen, devuelve la versión con ID".
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

        // "Cuando se mapee la entidad guardada, devuelve el DTO esperado".
        when(imageMapper.imageToImageResponseDTO(savedImage)).thenReturn(expectedResponseDTO);

        // --- ACT (Actuar) ---

        // 6. Llamamos al metodo que queremos probar.
        ImageResponseDTO result = imageService.createImage(createDTO);

        // --- ASSERT (Verificar) ---

        // 7. Verificamos que el resultado es el esperado.
        assertNotNull(result);
        assertEquals(100L, result.imageId());
        assertEquals("Imagen de café", result.imageName());

        // 8. Verificamos que el metodo save() fue llamado una vez.
        verify(imageRepository, times(1)).save(any(Image.class));
    }

    @Test
    void shouldThrowNotFound_whenCreatingImageForNonExistentTheme() {
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de entrada con un ID de temática que no existe.
        Long nonExistentThemeId = 99L;
        CreateImageDTO createDTO = new CreateImageDTO(
                "Imagen Fantasma",
                "Descripción...",
                "http://example.com/ghost.jpg",
                nonExistentThemeId
        );

        // 2. Configuramos el mock:
        // "Cuando se busque la temática padre por este ID, simula que no se encontró nada".
        when(themeRepository.findById(nonExistentThemeId)).thenReturn(Optional.empty());


        // --- ACT & ASSERT (Actuar y Verificar) ---

        // 3. Verificamos que al ejecutar el metodo, se lanza la excepción correcta.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    imageService.createImage(createDTO);
                }
        );

        // 4. Verificamos que el mensaje de la excepción es el que esperamos.
        assertEquals("Theme not found with id: " + nonExistentThemeId, exception.getMessage());

        // 5. Verificamos que el método save() del repositorio de imágenes NUNCA fue llamado.
        verify(imageRepository, never()).save(any(Image.class));
    }
}