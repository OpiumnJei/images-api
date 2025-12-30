package com.greetingsapp.imagesapi.domain.specialdays;

import com.greetingsapp.imagesapi.domain.common.AuditableBaseEntity;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "special_days")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpecialDay extends AuditableBaseEntity { //entidad que representa un día especial en la aplicación

    @Column(nullable = false)
    private String name;

    @Column(name = "day_month", nullable = false)
    private Integer day;

    @Column(name = "month_of_year", nullable = false)
    private Integer month;

    // Relación: Muchos Dias Especiales distintos pueden pertenecer a una misma temática (ToOne).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;
}