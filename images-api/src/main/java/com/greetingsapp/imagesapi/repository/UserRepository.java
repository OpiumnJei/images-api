package com.greetingsapp.imagesapi.repository;

import com.greetingsapp.imagesapi.domain.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    // Busca un usuario por su nombre de usuario.
    // Spring Security lo usará para cargar los detalles del usuario.
    Optional<User> findByUsername(String username);
}
