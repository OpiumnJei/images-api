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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Cacheable(value = "homeContent", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @RateLimiter(name = "publicApiRL")
    @CircuitBreaker(name = "databaseCB", fallbackMethod = "getHomeContentFallback")
    @Retry(name = "databaseRetry")
    public HomeContentDTO getHomeContent(Pageable pageable) {
        LocalDate today = LocalDate.now(ZoneId.of("America/Santo_Domingo"));

        Optional<SpecialDay> specialDay = specialDayRepository.findByDayAndMonth(
                today.getDayOfMonth(), today.getMonthValue());

        if (specialDay.isPresent()) {
            SpecialDay event = specialDay.get();
            Page<Image> imagePage = imageRepository.findByThemeId(event.getTheme().getId(), pageable);
            List<ImageResponseDTO> dtos = imageMapper.ImageDTOtoList(imagePage.getContent());
            return new HomeContentDTO("SPECIAL_EVENT", "Hoy es " + event.getName() + " ✨", dtos, imagePage.isLast(), imagePage.getTotalPages());
        }

        Page<Image> imagePage = imageRepository.findAll(pageable);
        List<ImageResponseDTO> dtos = imageMapper.ImageDTOtoList(imagePage.getContent());
        return new HomeContentDTO("DEFAULT", "Lo Último Agregado 🔥", dtos, imagePage.isLast(), imagePage.getTotalPages());
    }

    /**
     * Método fallback que se ejecuta cuando:
     * - El Circuit Breaker está ABIERTO (demasiados fallos recientes)
     * - Se agotaron los reintentos del Retry
     * <p>
     * Devuelve una respuesta degradada pero funcional para mantener
     * la experiencia del usuario aunque el sistema esté parcialmente caído.
     */
    private HomeContentDTO getHomeContentFallback(Pageable pageable, Exception ex) {
        log.error("Fallback activado en getHomeContent (page: {}, size: {}). Causa: {}", pageable.getPageNumber(), pageable.getPageSize(), ex.getMessage());
        return new HomeContentDTO("FALLBACK", "Contenido temporalmente no disponible 🔄", Collections.emptyList(), true, 0);
    }
}