package com.greetingsapp.imagesapi.domain.images;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.common.AuditableBaseEntity;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "images")
@AllArgsConstructor
@NoArgsConstructor
public class Image extends AuditableBaseEntity {

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "url", nullable = false)
    private String url;

    // Muchas imagenes (Many) pueden pertenecer a una tematica (ToOne).
    // @JoinColumn crea la columna de la llave foránea en la tabla images en la bd
    @ManyToOne(fetch = FetchType.LAZY) //fetchType usado para la carga de datos desde la bd
    @JoinColumn(name = "theme_id", nullable = false) //nombre del campo que hace referencia a la fk
    private Theme theme;


}
