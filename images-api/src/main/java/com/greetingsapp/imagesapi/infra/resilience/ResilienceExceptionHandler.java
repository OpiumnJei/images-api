package com.greetingsapp.imagesapi.infra.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Manejador global de excepciones relacionadas con los patrones de resiliencia.
 * Captura las excepciones lanzadas por Resilience4j y devuelve respuestas HTTP apropiadas.
 */
@RestControllerAdvice
public class ResilienceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ResilienceExceptionHandler.class);

    /**
     * Maneja la excepción cuando el Circuit Breaker está ABIERTO.
     * Esto ocurre cuando el servicio dependiente ha fallado demasiadas veces.
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<Map<String, Object>> handleCircuitBreakerOpen(CallNotPermittedException ex) {
        log.warn("Circuit Breaker OPEN - Servicio temporalmente no disponible: {}", ex.getCausingCircuitBreakerName());

        Map<String, Object> errorResponse = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Temporarily Unavailable",
                "message", "El servicio está temporalmente no disponible. Por favor, intente nuevamente en unos momentos.",
                "circuitBreaker", ex.getCausingCircuitBreakerName()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse);
    }

    /**
     * Maneja la excepción cuando el Rate Limiter rechaza la petición.
     * Esto ocurre cuando se ha excedido el límite de peticiones permitidas.
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Map<String, Object>> handleRateLimiterExceeded(RequestNotPermitted ex) {
        log.warn("Rate Limiter - Demasiadas peticiones: {}", ex.getMessage());

        Map<String, Object> errorResponse = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "error", "Too Many Requests",
                "message", "Ha excedido el límite de peticiones. Por favor, espere un momento antes de intentar nuevamente."
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "1") // Sugiere esperar 1 segundo
                .body(errorResponse);
    }
}
