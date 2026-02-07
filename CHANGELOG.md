# Changelog

Todos los cambios notables de este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Versionado Semántico](https://semver.org/lang/es/).

## [0.0.1] - 2026-02-01

### 🎉 Release Inicial

Primera versión estable de la API de Saludos con Imágenes.

### ✨ Añadido

#### Core

- **API RESTful completa** para gestión de imágenes, categorías y temáticas
- **Autenticación JWT** con roles diferenciados (ADMIN/CLIENT)
- **Paginación** en endpoints de listado para rendimiento óptimo
- **Documentación Swagger/OpenAPI** interactiva en `/swagger-ui/index.html`

#### Resiliencia (Resilience4j)

- **Circuit Breaker** para prevenir fallos en cascada
    - Umbral de fallo: 60%
    - Ventana deslizante: 10 llamadas
    - Tiempo en estado OPEN: 20 segundos
    - Fallbacks con datos de emergencia
- **Retry** con backoff exponencial (300ms → 600ms → 1200ms)
    - Máximo 3 reintentos para fallos transitorios
- **Rate Limiter** para protección contra sobrecarga
    - Endpoints públicos: 50 req/s
    - Endpoints admin: 100 req/s
    - Autenticación: 10 req/s (prevención brute force)

#### Base de Datos

- **MySQL 8.0** como motor de base de datos
- **Flyway** para migraciones versionadas
- **Optimización N+1** con @EntityGraph en consultas críticas
- **HikariCP** configurado para producción

#### DevOps

- **Docker & Docker Compose** para contenerización
- **Perfiles de Spring** (dev/prod) separados
- **Spring Boot Actuator** para monitoreo
- **Tests de carga con k6** para validar resiliencia

#### Seguridad

- **Spring Security** con filtros JWT personalizados
- **CORS configurable** por ambiente
- **Gestión de secretos** via variables de entorno
- **Manejo de errores centralizado** sin exponer stacktraces

### 🔧 Configuración

- Pool de conexiones HikariCP optimizado (20 max, 5 min idle)
- Compresión HTTP habilitada para respuestas
- Timeouts configurados para prevenir requests colgados
- Health checks para Kubernetes/Railway

### 📚 Documentación

- README.md con instrucciones de uso
- DEPLOYMENT.md con guía de despliegue
- k6-tests/README.md con documentación de tests de carga

---

## [Unreleased]

### Por hacer

- [ ] Integración con servicio de almacenamiento de imágenes (S3/CloudFlare)
- [ ] Cache con Redis para respuestas frecuentes
- [ ] Métricas con Prometheus/Grafana
- [ ] CI/CD con GitHub Actions

---

## Tipos de cambios

- `✨ Añadido` para nuevas funcionalidades
- `🔄 Cambiado` para cambios en funcionalidades existentes
- `⚠️ Deprecado` para funcionalidades que serán eliminadas
- `🗑️ Eliminado` para funcionalidades eliminadas
- `🐛 Corregido` para corrección de bugs
- `🔒 Seguridad` para vulnerabilidades corregidas
