# Plan de Refactorización: Paginación en API REST

## Auditoría Inicial

Fecha: 2026-07-27
Proyecto: images-api (Spring Boot 3.5.4, Java 17)

---

## Resumen de Hallazgos

### 🔴 Problemas Críticos

| # | Archivo | Línea | Problema | Impacto |
|---|---------|-------|----------|---------|
| 1 | `ImageService.java` | 62 | `@Cacheable("imagesByTheme")` sin SpEL `key` | Todas las combinaciones `(themeId, pageable)` comparten la misma entrada de caché → **datos incorrectos** entre páginas |
| 2 | `ImageService.java` | 73 | `@Cacheable("allImages")` sin `key` | Cada `pageable` distinto pisa la misma clave de caché |
| 3 | `ImageService.java` | 93 | `@Cacheable("imagesByCategory")` sin `key` | Cada `(categoryId, pageable)` colisiona en la misma entrada |
| 4 | `ThemeService.java` | 65 | `@Cacheable("themesByCategory")` sin `key` | El método recibe `categoryId` pero nunca se incluye en la clave de caché |

### 🟡 Problemas Moderados

| # | Archivo | Línea | Problema |
|---|---------|-------|----------|
| 5 | `ThemeController.java` | 28 | `Pageable pageable` sin `@PageableDefault` — el comportamiento por defecto es implícito y frágil |
| 6 | `HomeService.java` | 81-85 | `themeImages.stream().limit(20)` redundante: la BD ya devolvió solo 20 resultados vía `PageRequest.of(0, 20)` |

### 🟢 Observaciones (No requieren cambio)

| # | Archivo | Nota |
|---|---------|------|
| 7 | `CategoryController.java`, `ThemeService.java` | `getAllCategories()`, `getThemes()` retornan `List` sin paginación. Aceptable si el catálogo es pequeño (< 100 registros) |
| 8 | `ImageController.java`, `CategoryController.java` | Ya usan `@PageableDefault` correctamente ✅ |
| 9 | `ImageRepository.java` | Todos los métodos de paginación reciben `Pageable` y retornan `Page<T>` ✅ |
| 10 | `HomeService.java` | `@Cacheable("homeContent")` no necesita `key` porque el método no toma parámetros ✅ |

---

## Plan de Ejecución (5 Pasos)

### Paso 1: Corregir `@Cacheable` en `ImageService.java`

Archivo: `src/main/java/com/greetingsapp/imagesapi/services/ImageService.java`

| Método | Firma | Cache Actual | Cache Corregido |
|--------|-------|-------------|-----------------|
| `getImages` | `(Long themeId, Pageable pageable)` | `@Cacheable("imagesByTheme")` | `@Cacheable(value = "imagesByTheme", key = "#themeId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")` |
| `getAllImages` | `(Pageable pageable)` | `@Cacheable("allImages")` | `@Cacheable(value = "allImages", key = "#pageable.pageNumber + '-' + #pageable.pageSize")` |
| `getImagesByCategory` | `(Long categoryId, Pageable pageable)` | `@Cacheable("imagesByCategory")` | `@Cacheable(value = "imagesByCategory", key = "#categoryId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")` |

**Diferenciación en caché:** Cada combinación única de `(id, página, tamaño)` tendrá su propia entrada, evitando servir datos de una página cuando se solicita otra.

### Paso 2: Corregir `@Cacheable` en `ThemeService.java`

Archivo: `src/main/java/com/greetingsapp/imagesapi/services/ThemeService.java`

| Método | Firma | Cache Actual | Cache Corregido |
|--------|-------|-------------|-----------------|
| `getThemes` | `(Long categoryId)` | `@Cacheable("themesByCategory")` | `@Cacheable(value = "themesByCategory", key = "#categoryId")` |

**Ganancia:** Cada categoría obtiene su propia entrada de caché.

### Paso 3: Agregar `@PageableDefault` en `ThemeController.java`

Archivo: `src/main/java/com/greetingsapp/imagesapi/controller/ThemeController.java`

**Antes:**
```java
public ResponseEntity<Page<ImageResponseDTO>> getImagesByTheme(
        @PathVariable Long themeId, Pageable pageable);
```

**Después:**
```java
public ResponseEntity<Page<ImageResponseDTO>> getImagesByTheme(
        @PathVariable Long themeId,
        @PageableDefault(size = 20, sort = "created", direction = Sort.Direction.DESC) Pageable pageable);
```

**Ganancia:** Comportamiento explícito. Sin `@PageableDefault` el framework recurre a propiedades globales que pueden no estar configuradas.

### Paso 4: Eliminar `.stream().limit(20)` redundante en `HomeService.java`

Archivo: `src/main/java/com/greetingsapp/imagesapi/services/HomeService.java`

**Antes (líneas 80-86):**
```java
List<ImageResponseDTO> dtos = imageMapper.ImageDTOtoList(
        themeImages.stream().limit(20).toList());
```

**Después:**
```java
List<ImageResponseDTO> dtos = imageMapper.ImageDTOtoList(themeImages);
```

**Ganancia:** Código más limpio. El límite ya se aplicó en la consulta SQL mediante `PageRequest.of(0, 20)`. El `.stream().limit(20)` era redundante.

### Paso 5 (Opcional): Crear DTO de paginación personalizado

Archivo nuevo: `src/main/java/com/greetingsapp/imagesapi/dto/common/PagedResponseDTO.java`

Si se desea una estructura de respuesta más limpia que `Page<T>` de Spring Data (la cual expone metadatos internos como `pageable.sort.sorted`, `pageable.sort.unsorted`, etc.), se puede crear un record genérico:

```java
package com.greetingsapp.imagesapi.dto.common;

import java.util.List;
import org.springframework.data.domain.Page;

public record PagedResponseDTO<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {
    public static <T> PagedResponseDTO<T> from(Page<T> page) {
        return new PagedResponseDTO<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
```

**Ganancia:** Respuesta JSON predecible y desacoplada de Spring Data. Los controladores retornarían `ResponseEntity<PagedResponseDTO<ImageResponseDTO>>`.


### Paso 6: Home paginable con scroll infinito

**Archivos a modificar:**
- `services/HomeService.java`
- `controller/HomeController.java`
- `dto/home/HomeContentDTO.java`

**Objetivo:** Habilitar scroll infinito en la pantalla de inicio (`/api/home`) mediante paginación real en BD, tanto para días especiales como para días normales.

#### 6a. `HomeContentDTO.java` — Agregar metadata de paginación

```java
public record HomeContentDTO(
        String type,
        String title,
        List<ImageResponseDTO> images,
        boolean last,
        int totalPages
) {}
```

#### 6b. `HomeService.java` — Refactorizar método y fallback

```java
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

private HomeContentDTO getHomeContentFallback(Pageable pageable, Exception ex) {
    log.error("Fallback activado en getHomeContent (page: {}, size: {}). Causa: {}", pageable.getPageNumber(), pageable.getPageSize(), ex.getMessage());
    return new HomeContentDTO("FALLBACK", "Contenido temporalmente no disponible 🔄", Collections.emptyList(), true, 0);
}
```

**Cambios clave:**
- Parámetro `Pageable pageable` en lugar de `int page, int size`
- Eliminado `.stream().limit(20)` redundante
- `@Cacheable` ahora incluye `key` con `#pageable.pageNumber + '-' + #pageable.pageSize`
- Fallback actualizado para coincidir con nueva firma

#### 6c. `HomeController.java` — Aceptar `Pageable`

```java
@GetMapping
public ResponseEntity<HomeContentDTO> getHomeContent(
        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(homeService.getHomeContent(pageable));
}
```

---

## Resumen de Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `services/ImageService.java` | 3 anotaciones `@Cacheable` con nuevo `key` |
| `services/ThemeService.java` | 1 anotación `@Cacheable` con nuevo `key` |
| `controller/ThemeController.java` | Agregar `@PageableDefault` |
| `services/HomeService.java` | Recibir `Pageable`, eliminar `.stream().limit(20)`, nuevo `@Cacheable key`, nuevo fallback |
| `controller/HomeController.java` | Aceptar `Pageable` con `@PageableDefault` |
| `dto/home/HomeContentDTO.java` | Agregar campos `last` y `totalPages` |
| `dto/common/PagedResponseDTO.java` | **(Opcional)** Nuevo DTO genérico de paginación |

---

## Notas Técnicas

- **Spring Boot:** 3.5.4
- **Java:** 17
- **Caché:** Caffeine (máx. 600 entradas, expireAfterWrite=10m)
- Las claves SpEL usan concatenación con `'-'` para evitar colisiones (ej: `1-0-20` vs `10-2-0`)
- Los métodos con `@Cacheable` también tienen `@RateLimiter`, `@CircuitBreaker` y `@Retry` — el orden de ejecución de los aspectos es: `RateLimiter → CircuitBreaker → Retry → Cacheable → Método`
