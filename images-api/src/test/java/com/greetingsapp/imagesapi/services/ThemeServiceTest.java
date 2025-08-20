package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import com.greetingsapp.imagesapi.domain.themes.ThemeMapper;
import com.greetingsapp.imagesapi.dto.themes.CreateThemeDTO;
import com.greetingsapp.imagesapi.dto.themes.ThemeResponseDTO;
import com.greetingsapp.imagesapi.infra.errors.ResourceNotFoundException;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
import com.greetingsapp.imagesapi.repository.ThemeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ThemeMapper themeMapper;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void shouldReturnThemes_whenCategoryExists() {
        // --- ARRANGE (Preparar el escenario) ---
        Long categoryId = 1L;
        Theme themeEntity = new Theme(); // Entidad falsa
        ThemeResponseDTO themeDTO = new ThemeResponseDTO(10L, "Navidad"); // DTO falso

        // Le decimos a los mocks cómo comportarse:
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(themeRepository.findByCategoryId(categoryId)).thenReturn(List.of(themeEntity));
        when(themeMapper.themeListToThemeResponseDTOList(anyList())).thenReturn(List.of(themeDTO));

        // --- ACT (Actuar) ---
        List<ThemeResponseDTO> result = themeService.getThemes(categoryId);

        // --- ASSERT (Verificar) ---
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Navidad", result.get(0).themeName());
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(themeRepository, times(1)).findByCategoryId(categoryId);
        verify(themeMapper, times(1)).themeListToThemeResponseDTOList(anyList());
    }

    @Test
    void shouldThrowResourceNotFound_whenCategoryDoesNotExist() {
        // --- ARRANGE ---
        Long categoryId = 99L; // Un ID que no existe
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // --- ACT & ASSERT ---
        // Verificamos que al ejecutar el metodo, se lanza la excepción esperada
        assertThrows(ResourceNotFoundException.class, () -> {
            themeService.getThemes(categoryId);
        });

        // Verificamos que NUNCA se intentó buscar temáticas,
        // porque la validación de la categoría falló primero.
        verify(themeRepository, never()).findByCategoryId(any());
    }

    @Test
    void shouldReturnEmptyList_whenCategoryHasNoThemes() {
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de prueba.
        Long categoryId = 1L;

        // 2. Configuramos el comportamiento de los mocks.
        // "Cuando se verifique si la categoría existe, responde que sí".
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        // "Cuando se busquen las temáticas, simula que la base de datos devuelve una lista vacía".
        when(themeRepository.findByCategoryId(categoryId)).thenReturn(Collections.emptyList());

        // "Cuando el mapper intente convertir la lista vacía, devuelve otra lista vacía de DTOs".
        when(themeMapper.themeListToThemeResponseDTOList(anyList())).thenReturn(Collections.emptyList());


        // --- ACT (Actuar) ---

        // 3. Llamamos al metodo que queremos probar.
        List<ThemeResponseDTO> result = themeService.getThemes(categoryId);


        // --- ASSERT (Verificar) ---

        // 4. Verificamos que el resultado no sea nulo y que esté vacío.
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 5. Verificamos que los métodos de los repositorios fueron llamados.
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(themeRepository, times(1)).findByCategoryId(categoryId);

    }

    @Test
    void shouldCreateTheme_whenDataIsValid(){
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de entrada.
        CreateThemeDTO createDTO = new CreateThemeDTO("Nueva Temática", 1L);

        // 2. Simula la categoría padre que existe en la BD.
        Category parentCategory = new Category();
        parentCategory.setId(1L);

        // 3. Simula la entidad Theme que se va a guardar.
        Theme savedTheme = new Theme();
        savedTheme.setId(1L); // ID simulado después de guardar
        savedTheme.setName(createDTO.name());
        savedTheme.setCategory(parentCategory);

        // 4. El DTO de respuesta esperado
        ThemeResponseDTO expectedResponseDTO = new ThemeResponseDTO(
                savedTheme.getId(),
                savedTheme.getName());

        // 5. Configuramos el comportamiento de los mocks.
        // "Cuando se busque la categoría padre por ID, encuéntrala".
        when(categoryRepository.findById(createDTO.categoryId())).thenReturn(Optional.of(parentCategory));

        // "Cuando se verifique si el nombre ya existe en esa categoría, di que no".
        when(themeRepository.findByNameAndCategory(createDTO.name(), parentCategory)).thenReturn(Optional.empty());

        // "Cuando se guarde la nueva temática, devuelve la versión con ID".
        when(themeRepository.save(any(Theme.class))).thenReturn(savedTheme);

        // "Cuando se mapee la entidad guardada, devuelve el DTO esperado".
        when(themeMapper.themeToThemeResponseDTO(savedTheme)).thenReturn(expectedResponseDTO);


        // --- ACT (Actuar) ---

        // 6. Llamamos al metodo que queremos probar.
        ThemeResponseDTO result = themeService.createTheme(createDTO);

        // --- ASSERT (Verificar) ---

        // 7. Verificamos que el resultado es el esperado.
        assertNotNull(result);
        assertEquals(1L, result.themeId());
        assertEquals("Nueva Temática", result.themeName());

        // 8. Verificamos que el metodo save() fue llamado una vez.
        verify(themeRepository, times(1)).save(any(Theme.class));
    }

    @Test
    void shouldThrowNotFound_whenCreatingThemeForNonExistentCategory() {
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de entrada con un ID de categoría que no existe.
        Long nonExistentCategoryId = 99L;
        CreateThemeDTO createDTO = new CreateThemeDTO("Temática Fantasma", nonExistentCategoryId);

        // 2. Configuramos el mock:
        // "Cuando se busque la categoría padre por este ID, simula que no se encontró nada".
        when(categoryRepository.findById(nonExistentCategoryId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT (Actuar y Verificar) ---

        // 3. Verificamos que al ejecutar el metodo, se lanza la excepción correcta.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    themeService.createTheme(createDTO);
                }
        );

        // 4. Verificamos que el mensaje de la excepción es el que esperamos.
        assertEquals("Category not found with id: " + nonExistentCategoryId, exception.getMessage());

        // 5. Verificamos que el metodo save() del repositorio de temáticas NUNCA fue llamado.
        verify(themeRepository, never()).save(any(Theme.class));
    }
}