package com.greetingsapp.imagesapi.domain.categories;

import com.greetingsapp.imagesapi.domain.common.AuditableBaseEntity;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true) //si un Theme es eliminado de la lista themes, ese Theme es considerado un "huérfano" y debe ser eliminado de la base de datos.
    private List<Theme> themes;

}
