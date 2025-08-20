package com.greetingsapp.imagesapi.repository;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//capa de acceso a datos (los repositorios)
// Category este es el tipo de entidad para la cual el repositorio está diseñado.
// Long Este es el tipo de dato de la clave primaria (ID) de la entidad Category.
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
}
