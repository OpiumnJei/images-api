package com.greetingsapp.imagesapi.repository;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//capa de acceso a datos (los repositorios)
// Theme este es el tipo de entidad para la cual el repositorio está diseñado.
// Long Este es el tipo de dato de la clave primaria (ID) de la entidad Theme.
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    //busca el campo categoryId dentro de la tematica, retorna todas las tematicas,
    // que pertenecen a una categoria en especifico
    List<Theme> findByCategoryId(Long categoryId);

    // Busca una temática por su nombre Y su objeto de categoría asociado
    // Esto para buscar los/el nombre/s de una/s tematica asociada a una categoria especifica
    // Tambien esto proporciona cierta flexibilidad a la hora crear tematicas con el mismo nombre,
    // pero en diferentes categorias
    Optional<Theme> findByNameAndCategory(String name, Category category);
}
