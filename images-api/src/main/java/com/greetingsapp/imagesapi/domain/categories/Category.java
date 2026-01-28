package com.greetingsapp.imagesapi.domain.categories;

import com.greetingsapp.imagesapi.domain.common.AuditableBaseEntity;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "categories")
@AllArgsConstructor
@NoArgsConstructor
public class Category extends AuditableBaseEntity {

    //unique = true: El valor en la columna name debe ser único en esa tabla
    //nullable = false: La columna name no puede quedar vacía
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    //Una categoria (one) puede tener muchas tematicas (ToMany).
    // "mappedBy" le dice a JPA: "No crees una columna para esta lista aquí.
    // La configuración de la unión ya está definida en el campo 'category' de la clase Theme."
    // fetch = LAZY: Las temáticas NO se cargan automáticamente (evita N+1).
    // Solo se cargan si se accede explícitamente a getThemes().
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Theme> themes;

}
