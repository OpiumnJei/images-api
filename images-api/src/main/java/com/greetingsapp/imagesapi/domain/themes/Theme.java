package com.greetingsapp.imagesapi.domain.themes;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.common.AuditableBaseEntity;
import com.greetingsapp.imagesapi.domain.images.Image;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.boot.archive.scan.spi.ClassDescriptor;
import org.springframework.data.domain.Auditable;

import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "themes")
@AllArgsConstructor
@NoArgsConstructor
public class Theme extends AuditableBaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Muchas tematicas (Many) pueden pertenecer a una categoria (ToOne).
    // @JoinColumn crea la columna de la llave foránea en la tabla themes en la bd
    @ManyToOne(fetch = FetchType.LAZY) //fetchType usado para la carga de datos desde la bd
    @JoinColumn(name = "category_id", nullable = false) //nombre del campo que hace referencia a la fk
    private Category category;

    // Una tematica (one) puede tener muchas imagenes(ToMany)
    // "mappedBy" le dice a JPA: "No crees una columna para esta lista aquí."
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

}
