package com.greetingsapp.imagesapi.repository;

import com.greetingsapp.imagesapi.domain.specialdays.SpecialDay;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Repositorio para manejar los días especiales (festivos, celebraciones, etc.)
public interface SpecialDayRepository extends JpaRepository<SpecialDay, Long> {

    /**
     * Buscar si existe algo para el día y mes exactos.
     *
     * @EntityGraph evita el problema N+1 al cargar el Theme en la misma query.
     * Sin esto, acceder a specialDay.getTheme() dispararía una query adicional.
     */
    @EntityGraph(attributePaths = {"theme"})
    Optional<SpecialDay> findByDayAndMonth(Integer day, Integer month);

    // (Opcional) Si quisieras manejar fechas móviles (ej: 3er domingo de junio),
    // la lógica se haría en el servicio, aquí solo traemos por fecha fija por ahora.
}