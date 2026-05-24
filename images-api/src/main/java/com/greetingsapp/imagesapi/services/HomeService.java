package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.images.Image;
import com.greetingsapp.imagesapi.domain.images.ImageMapper;
import com.greetingsapp.imagesapi.domain.specialdays.SpecialDay;
import com.greetingsapp.imagesapi.dto.home.HomeContentDTO;
import com.greetingsapp.imagesapi.dto.images.ImageResponseDTO;
import com.greetingsapp.imagesapi.repository.ImageRepository;
import com.greetingsapp.imagesapi.repository.SpecialDayRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Servicio para manejar la lógica de la página de inicio
@Service
public class HomeService {

    private static final Logger log = LoggerFactory.getLogger(HomeService.class);

    @Autowired
    private SpecialDayRepository specialDayRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageMapper imageMapper;

    /**
     * Obtiene el contenido dinámico de la página de inicio.
     * <p>
     * Patrones de resiliencia aplicados:
     * - @RateLimiter: Limita a 50 peticiones/segundo para proteger el servidor
     * - @CircuitBreaker: Previene fallos en cascada si la BD está caída
     * - @Retry: Reintenta automáticamente ante fallos transitorios de red/BD
     * <p>
     * El orden de ejecución es: RateLimiter → CircuitBreaker → Retry → Método
     */
    @Cacheable(value = "homeContent") //se encarga de crear un espacio en memoria para las queries a este metodo
    @RateLimiter(name = "publicApiRL")
    @CircuitBreaker(name = "databaseCB", fallbackMethod = "getHomeContentFallback")
    @Retry(name = "databaseRetry")
    public HomeContentDTO getHomeContent() {
//        log.info("⚠️ [CACHE MISS] ¡Oh no! El dato no estaba en caché. Buscando en la Base de Datos...");
//        log.debug("Obteniendo contenido del home...");

        // Forzamos a que calcule 'hoy' basado en la zona horaria deseada
        LocalDate today = LocalDate.now(ZoneId.of("America/Santo_Domingo"));

        // 1. Preguntar: ¿Hay algo especial hoy (dia, mes)?
        Optional<SpecialDay> specialDay = specialDayRepository.findByDayAndMonth(
                today.getDayOfMonth(), // día del mes
                today.getMonthValue() // mes del año
        );

        if (specialDay.isPresent()) { // si se encuentra un día especial, es decir, que coincide tanto el dia como el mes
            // --- CASO 1: ES UN DÍA ESPECIAL ---
            SpecialDay event = specialDay.get(); // obtenemos t0do el objeto SpecialDay

            Long themeId = event.getTheme().getId();  // obtenemos el id de la tematica asociado a ese día especial

            Pageable limit = PageRequest.of(0, 20); // Creamos un objeto Pageable para limitar a 20 resultados
            // Buscamos las imágenes asociadas al TEMA de ese día especial
            List<Image> themeImages = imageRepository.findByThemeId(themeId, limit).getContent();

            // Limitamos a 20 para no saturar si hay muchas
            List<ImageResponseDTO> dtos = imageMapper.ImageDTOtoList(
                    themeImages
                            .stream()
                            .limit(20)
                            .toList()
            );

            // Retornamos el contenido especial para hoy
            return new HomeContentDTO(
                    "SPECIAL_EVENT",
                    "Hoy es " + event.getName() + " ✨", // Ej: Hoy es Navidad ✨
                    dtos
            );

        } else {
            // --- CASO 2: DÍA NORMAL (MOSTRAR NOVEDADES) ---

            // Buscamos las últimas 20 imágenes subidas al sistema en general
            // Usamos Paging para traer solo 20 ordenadas por ID descendente (o created date)
            // PageRequest es el control remoto de paginación
            List<Image> recentImages = imageRepository.findAll(
                    PageRequest.of(
                            0,
                            20,
                            Sort.by(Sort.Direction.DESC, "id"))
            ).getContent(); //obtemos los datos crudos de la paginacion

            List<ImageResponseDTO> dtos = imageMapper.ImageDTOtoList(recentImages);

            return new HomeContentDTO(
                    "DEFAULT",
                    "Lo Último Agregado 🔥",
                    dtos
            );
        }
    }

    /**
     * Método fallback que se ejecuta cuando:
     * - El Circuit Breaker está ABIERTO (demasiados fallos recientes)
     * - Se agotaron los reintentos del Retry
     * <p>
     * Devuelve una respuesta degradada pero funcional para mantener
     * la experiencia del usuario aunque el sistema esté parcialmente caído.
     */
    private HomeContentDTO getHomeContentFallback(Exception ex) {
        log.error("Fallback activado en getHomeContent. Causa: {}", ex.getMessage());

        // Respuesta degradada: contenido estático de emergencia
        return new HomeContentDTO(
                "FALLBACK",
                "Contenido temporalmente no disponible 🔄",
                Collections.emptyList() // Lista vacía en lugar de null
        );
    }
}