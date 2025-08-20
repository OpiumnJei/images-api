package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.categories.CategoryMapper;
import com.greetingsapp.imagesapi.dto.categories.CategoryResponseDTO;
import com.greetingsapp.imagesapi.dto.categories.CreateCategoryDTO;
import com.greetingsapp.imagesapi.dto.categories.UpdateCategoryDTO;
import com.greetingsapp.imagesapi.infra.errors.DuplicateResourceException;
import com.greetingsapp.imagesapi.infra.errors.ResourceNotFoundException;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
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

@ExtendWith(MockitoExtension.class) // Activa la magia de Mockito
class CategoryServiceTest {

    @Mock // 1. Crea una simulación (un "impostor") de CategoryRepository
    private CategoryRepository categoryRepository;

    @Mock // 2. Crea una simulación de CategoryMapper
    private CategoryMapper categoryMapper;

    @InjectMocks // 3. Crea una instancia real de CategoryService e inyéctale los mocks de arriba
    private CategoryService categoryService;


    @Test
    void shouldReturnListOfCategoryResponseDTO_whenCategoryExists() { //Convencion should/when para nombrar metodoss
        // --- ARRANGE (Preparar el escenario) ---

        // Preparamos los datos de prueba
        Category category1 = new Category(); // Objeto Entidad
        category1.setId(1L);
        category1.setName("Buenos Días");

        CategoryResponseDTO dto1 = new CategoryResponseDTO(1L, "Buenos Días"); // Objeto DTO

        // Le decimos al mock del repositorio qué debe hacer:
        // "Cuando alguien llame al metodo findAll(), devuelve la lista con category1"
        when(categoryRepository.findAll()).thenReturn(List.of(category1));

        // Le decimos al mock del mapper qué debe hacer:
        // "Cuando alguien llame al metodo de mapeo con la lista de entidades, devuelve la lista con dto1"
        when(categoryMapper.categoryListToCategoryResponseDTOList(List.of(category1)))
                .thenReturn(List.of(dto1));


        // --- ACT (Actuar) ---

        // Llamamos al metodo que queremos probar
        List<CategoryResponseDTO> resultado = categoryService.getAllCategories();


        // --- ASSERT (Verificar) ---

        // Verificamos que el resultado no sea nulo y que contenga 1 elemento
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        // Verificamos que el nombre del DTO en el resultado sea el esperado
        assertEquals("Buenos Días", resultado.get(0).categoryName());

        // Opcional: Verifica que los métodos de los mocks fueron llamados una vez
        verify(categoryRepository, times(1)).findAll();
        verify(categoryMapper, times(1)).categoryListToCategoryResponseDTOList(any());
    }

    @Test
    void shouldReturnEmptyList_whenNoCategoriesExist() {
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Le decimos al mock del repositorio que devuelva una lista vacía
        //    cuando se llame a su metodo findAll().
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        // 2. Le decimos al mock del mapper que, si recibe una lista vacía,
        //    debe devolver también una lista vacía de DTOs.
        when(categoryMapper.categoryListToCategoryResponseDTOList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());


        // --- ACT (Actuar) ---

        // 3. Llamamos al metodo que queremos probar.
        List<CategoryResponseDTO> result = categoryService.getAllCategories();


        // --- ASSERT (Verificar) ---

        // 4. Verificamos que el resultado no sea nulo.
        assertNotNull(result);

        // 5. Verificamos que la lista esté efectivamente vacía. ¡Esta es la prueba clave!
        assertTrue(result.isEmpty());

        // 6. (Opcional) Verificamos que los métodos de nuestros mocks
        //    fueron llamados exactamente una vez.
        verify(categoryRepository, times(1)).findAll();
        verify(categoryMapper, times(1)).categoryListToCategoryResponseDTOList(anyList());
    }

    @Test
    void shouldCreateCategory_whenNameIsValid() {
        // Preparamos los datos de prueba
        // arrange
        // 1. Preparamos el DTO de entrada (lo que enviaría el cliente).
        CreateCategoryDTO createDTO = new CreateCategoryDTO("Buenos Dias");

        // 2. Preparamos la entidad que esperamos que se guarde.
        Category categoryToSave = new Category();
        categoryToSave.setName(createDTO.name());

        // 3. Preparamos la entidad guardada (con el ID que simulará la BD).
        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName(createDTO.name());

        // 4. Preparamos el DTO de respuesta esperado.
        CategoryResponseDTO expectedResponseDTO = new CategoryResponseDTO(1L, "Buenos Días");

        // 5. Configuramos el comportamiento de los mocks.
        // "Cuando se busque por nombre, simula que no existe (para pasar la validación)".
        when(categoryRepository.findByName("Buenos Dias")).thenReturn(Optional.empty());
        // "Cuando se guarde CUALQUIER objeto Category, devuelve la versión con ID".
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        // "Cuando se mapee la entidad guardada, devuelve el DTO de respuesta esperado".
        when(categoryMapper.categoryToCategoryResponseDTO(savedCategory)).thenReturn(expectedResponseDTO);

        // --- ACT (Actuar) ---

        // 6. Llamamos al metodo que queremos probar.
        CategoryResponseDTO result = categoryService.createCategory(createDTO);

        // --- ASSERT (Verificar) ---

        // 7. Verificamos que el resultado no sea nulo y sea el esperado.
        assertNotNull(result);
        assertEquals("Buenos Días", result.categoryName());
        assertEquals(1L, result.categoryId());

        // 8. Verificamos que el metodo save() del repositorio fue llamado exactamente una vez.
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void shouldThrowException_whenCreatingCategoryWithExistingName() {

        // --- ARRANGE (Preparar el escenario) ---

        // 1. Preparamos el DTO de entrada con un nombre que ya existe.
        CreateCategoryDTO createDTO = new CreateCategoryDTO("Buenos Días");

        // 2. Creamos una entidad falsa para simular la que ya está en la BD.
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Buenos Días");

        // 3. Configuramos el mock:
        // "Cuando el repositorio busque por el nombre 'Buenos Días',
        // simula que SÍ encontró una categoría".
        when(categoryRepository.findByName("Buenos Días")).thenReturn(Optional.of(existingCategory));


        // --- ACT & ASSERT (Actuar y Verificar) ---

        // 4. Ejecutamos el metodo y verificamos que lanza la excepción esperada.
        // assertThrows captura la excepción para que podamos inspeccionarla.
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> {
                    // La acción que debe causar el error va aquí dentro.
                    categoryService.createCategory(createDTO);
                }
        );

        // 5. Verificamos que el mensaje de la excepción es el correcto.
        assertEquals("Category with name 'Buenos Días' already exists.", exception.getMessage());

        // 6. Verificamos que el metodo save() del repositorio NUNCA fue llamado,
        // porque la validación falló antes. ¡Esto es muy importante!
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldUpdateCategory_whenCategoryExists() {
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de entrada para la actualización.
        Long categoryId = 1L;
        UpdateCategoryDTO updateDTO = new UpdateCategoryDTO("Nombre Actualizado");

        // 2. Simula la categoría que ya existe en la base de datos.
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Nombre Original");

        // 3. Simula la entidad después de ser guardada con el nuevo nombre.
        Category savedCategory = new Category();
        savedCategory.setId(categoryId);
        savedCategory.setName(updateDTO.name());

        // 4. El DTO de respuesta que esperamos recibir.
        CategoryResponseDTO expectedResponseDTO = new CategoryResponseDTO(categoryId, updateDTO.name());

        // 5. Configuramos el comportamiento de los mocks.
        // "Cuando se busque la categoría por ID, devuelve la que ya existe".
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        // "Cuando se busque por el nuevo nombre, simula que no existe (para pasar la validación)".
        when(categoryRepository.findByName(updateDTO.name())).thenReturn(Optional.empty());

        // "Cuando se guarde CUALQUIER objeto Category, devuelve la versión ya actualizada".
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // "Cuando se mapee la entidad guardada, devuelve el DTO de respuesta esperado".
        when(categoryMapper.categoryToCategoryResponseDTO(savedCategory)).thenReturn(expectedResponseDTO);


        // --- ACT (Actuar) ---

        // 6. Llamamos al metodo que queremos probar.
        CategoryResponseDTO result = categoryService.updateCategory(categoryId, updateDTO);


        // --- ASSERT (Verificar) ---

        // 7. Verificamos que el resultado no sea nulo y tenga los datos actualizados.
        assertNotNull(result);
        assertEquals(categoryId, result.categoryId());
        assertEquals("Nombre Actualizado", result.categoryName());

        // 8. Verificamos que los métodos clave de los mocks fueron llamados.
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryMapper, times(1)).categoryToCategoryResponseDTO(any(Category.class));
    }

    @Test
    void shouldThrowNotFound_whenUpdatingNonExistentCategory() {
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de entrada para una categoría que no existe.
        Long nonExistentId = 99L;
        UpdateCategoryDTO updateDTO = new UpdateCategoryDTO("Nombre Fantasma");

        // 2. Configuramos el mock:
        // "Cuando se busque por este ID, simula que no se encontró nada".
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());


        // --- ACT & ASSERT (Actuar y Verificar) ---

        // 3. Verificamos que al ejecutar el metodo, se lanza la excepción correcta.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    // La acción que debe causar el error va aquí dentro.
                    categoryService.updateCategory(nonExistentId, updateDTO);
                }
        );

        // 4. Verificamos que el mensaje de la excepción es el que esperamos.
        assertEquals("Category with ID " + nonExistentId + " not found.", exception.getMessage());

        // 5. Verificamos que los métodos de escritura y mapeo NUNCA fueron llamados,
        //  porque el código falló antes, como debería.
        verify(categoryRepository, times(1)).findById(nonExistentId); // Se intentó buscar
        verify(categoryRepository, never()).save(any(Category.class)); // Pero nunca se guardó
        verify(categoryMapper, never()).categoryToCategoryResponseDTO(any(Category.class)); // Ni se mapeó
    }

    @Test
    void shouldDeleteCategory_whenCategoryExists() {
        // --- ARRANGE (Preparar el escenario) ---
        Long categoryId = 1L;

        // 1. Simulamos que la categoría SÍ existe en la base de datos.
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        // 2. Le decimos a Mockito que no haga nada cuando se llame a deleteById,
        //    ya que es un método void. Esto es opcional pero es buena práctica.
        doNothing().when(categoryRepository).deleteById(categoryId);

        // --- ACT (Actuar) ---

        // 3. Llamamos al método que queremos probar.
        // Usamos assertDoesNotThrow para asegurarnos de que no se lance ninguna excepción.
        assertDoesNotThrow(() -> {
            categoryService.deleteCategory(categoryId);
        });

        // --- ASSERT (Verificar) ---

        // 4. Verificamos que el método deleteById() del repositorio fue llamado
        //    exactamente una vez con el ID correcto.
        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    void shouldThrowNotFound_whenDeletingNonExistentCategory() {
        // --- ARRANGE (Preparar el escenario) ---
        Long nonExistentId = 99L;

        // 1. Simulamos que la categoría NO existe en la base de datos.
        when(categoryRepository.existsById(nonExistentId)).thenReturn(false);

        // --- ACT & ASSERT (Actuar y Verificar) ---

        // 2. Verificamos que al ejecutar el método, se lanza la excepción correcta.
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory(nonExistentId);
        });

        // 3. Verificamos que el método deleteById() NUNCA fue llamado,
        //    porque la validación falló primero.
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}
