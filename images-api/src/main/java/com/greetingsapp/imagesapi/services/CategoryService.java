package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.categories.CategoryMapper;
import com.greetingsapp.imagesapi.dto.categories.CategoryResponseDTO;
import com.greetingsapp.imagesapi.dto.categories.CreateCategoryDTO;
import com.greetingsapp.imagesapi.dto.categories.UpdateCategoryDTO;
import com.greetingsapp.imagesapi.infra.errors.DuplicateResourceException;
import com.greetingsapp.imagesapi.infra.errors.ResourceNotFoundException;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service //marcamos la clase como un servicio/componente de spring
public class CategoryService {

    // con autowired entablamos una relacion con el repositorio de categorias, lo inyectamos
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    //metodo para mostar todas las categorias disponibles al cliente
    public List<CategoryResponseDTO> getAllCategories() {

        // 1. Obtienes las entidades de la base de datos
        List<Category> categories = categoryRepository.findAll();

        // 2. Usas el mapper para convertirlas a DTOs antes de devolverlas
        return categoryMapper.categoryListToCategoryResponseDTOList(categories);
    }

    //--- Metodo para crear una nueva categoria ---
    public CategoryResponseDTO createCategory(CreateCategoryDTO createCategoryDTO) {

        // --- VALIDACION (Buena práctica) ---
        // Verificar si ya existe una categoría con ese nombre para evitar duplicados.
        if (categoryRepository.findByName(createCategoryDTO.name()).isPresent()) {
            throw new DuplicateResourceException("Category with name '" + createCategoryDTO.name() + "' already exists.");
        }

        // 1. Mapear del DTO de entrada a la Entidad
        Category newCategory = new Category();
        newCategory.setName(createCategoryDTO.name());

        // 2. GUARDAR la nueva entidad en la base de datos (¡Este paso faltaba!)
        Category savedCategory = categoryRepository.save(newCategory);

        // 3. Mapear la entidad guardada (que ahora tiene un ID) al DTO de respuesta
        return categoryMapper.categoryToCategoryResponseDTO(savedCategory);
    }


    // --- Metodo para actualizar una categoria ---
    @Transactional // Anotación clave para operaciones de escritura
    public CategoryResponseDTO updateCategory(Long categoryId, UpdateCategoryDTO updateCategoryDTO) {

        // 1. Busca la categoría existente en la base de datos.
        // Si no la encuentra, lanza una excepción 404.
        Category categoryToUpdate = getCategory(categoryId);

        // 2. --- VALIDACIÓN DE DUPLICIDAD ---
        // Busca si ya existe una categoría con el nuevo nombre.
        Optional<Category> existingCategory = categoryRepository.findByName(updateCategoryDTO.name());

        // Si existe, y su ID es DIFERENTE al de la categoría que estamos actualizando,
        // entonces es un conflicto.
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
            throw new DuplicateResourceException("Category name '" + updateCategoryDTO.name() + "' is already in use by another category.");
        }

        // 3. Actualiza los campos de la entidad.
        categoryToUpdate.setName(updateCategoryDTO.name());

        // 4. Guarda la entidad (JPA hará un UPDATE).
        Category updatedCategory = categoryRepository.save(categoryToUpdate);

        // 5. Mapea y devuelve el DTO.
        return categoryMapper.categoryToCategoryResponseDTO(updatedCategory);
    }

    //--- Metodo para eliminar una categoria ---
    @Transactional
    public void deleteCategory(Long categoryId) {

        // 1. VALIDACIÓN: Primero, comprueba si la categoría existe.
        if (!categoryRepository.existsById(categoryId)) {
            // Si no existe, lanza la excepción para devolver un 404.
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }

        // 2. ACCIÓN: Si existe, bórrala directamente por su ID.
        // No es necesario hacer un findById() primero.
        categoryRepository.deleteById(categoryId);
    }


    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + categoryId + " not found."));
    }

}

