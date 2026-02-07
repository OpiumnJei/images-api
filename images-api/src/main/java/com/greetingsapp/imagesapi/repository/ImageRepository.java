package com.greetingsapp.imagesapi.repository;

import com.greetingsapp.imagesapi.domain.images.Image;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//Al extender de `PagingAndSortingRepository`, le estás diciendo a otros desarrolladores
// (y a tu "yo" del futuro) que el propósito principal de
// este repositorio es manejar grandes cantidades de datos que necesitan ser paginados y ordenados.
public interface ImageRepository extends JpaRepository<Image, Long> {

    // retorna una pagina que representara una imagen
    Page<Image> findByThemeId(Long themeId, Pageable pageable);

    // Busca una imagen por su nombre Y su objeto de temática asociado.
    Optional<Image> findByNameAndTheme(String name, Theme theme);

    // Búsqueda flexible
    // Traduccción: "Encuentra imágenes donde el (Nombre contenga X) O (Descripción contenga X), ignorando mayúsculas/minúsculas"
    Page<Image> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    // Busca imágenes donde la propiedad 'category.id' dentro de 'theme' coincida con el parámetro.
    // Spring Data traduce esto automáticamente a un JOIN.
    Page<Image> findByThemeCategoryId(Long categoryId, Pageable pageable);

}
