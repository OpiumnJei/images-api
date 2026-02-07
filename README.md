# API de Saludos con Imágenes (images-api)

![Version](https://img.shields.io/badge/version-0.0.1-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-blue?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green?style=for-the-badge&logo=spring)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange?style=for-the-badge&logo=mysql)
![Resilience4j](https://img.shields.io/badge/Resilience4j-2.2-red?style=for-the-badge)
![k6](https://img.shields.io/badge/k6-Testing-purple?style=for-the-badge&logo=k6)

API RESTful construida con **Spring Boot** y totalmente contenerizada con **Docker**. Diseñada para ser el backend de
una aplicación móvil de saludos, permitiendo la gestión completa de imágenes, categorías y temáticas.

**🛡️ Diseñada con tolerancia a fallos** usando patrones de resiliencia empresariales.

---

## 🚀 Características Principales

* **🔐 Autenticación y Autorización con JWT:** Endpoints de administrador protegidos usando JSON Web Tokens, con roles
  diferenciados (`ADMIN`, `CLIENT`).
* **📦 CRUD Completo:** Operaciones para crear, leer, actualizar y eliminar categorías, temáticas e imágenes.
* **📄 Paginación:** La obtención de listas de imágenes está paginada para un rendimiento eficiente.
* **🏗️ Arquitectura en capas:** Clara separación de responsabilidades (Controladores, Servicios, Repositorios).
* **📚 Documentación con Swagger (OpenAPI):** Documentación de la API generada automáticamente y accesible de forma
  interactiva.
* **🐳 Entorno Dockerizado:** La aplicación y su base de datos se levantan con un solo comando gracias a
  `docker-compose`.

### 🛡️ Patrones de Resiliencia (Resilience4j)

* **⚡ Circuit Breaker:** Previene fallos en cascada cuando la base de datos o servicios externos fallan. Abre el
  circuito
  tras 60% de fallos y responde con datos de emergencia (fallback).
* **🔄 Retry:** Reintenta automáticamente operaciones fallidas con backoff exponencial (300ms → 600ms → 1200ms).
* **🚦 Rate Limiter:** Protege contra sobrecarga limitando peticiones:
    - Endpoints públicos: 50 req/s
    - Endpoints admin: 100 req/s
    - Autenticación: 10 req/s (prevención brute force)

---

## 🛠️ Stack Tecnológico

| Backend         | Base de Datos | Resiliencia     | Contenerización | Testing    |
|:----------------|:--------------|:----------------|:----------------|:-----------|
| Java 17         | MySQL 8.0     | Resilience4j    | Docker          | JUnit 5    |
| Spring Boot 3.5 | Flyway        | Circuit Breaker | Docker Compose  | Mockito    |
| Spring Security |               | Retry           |                 | k6 (carga) |
| Spring Actuator |               | Rate Limiter    |                 |            |
| Maven           |               |                 |                 |            |

---

## 🏁 Cómo Empezar

Para levantar el proyecto en tu entorno local, solo necesitas tener instalados **Docker** y **Docker Compose**.

### Prerrequisitos

* [Docker](https://docs.docker.com/get-docker/)
* [Docker Compose](https://docs.docker.com/compose/install/)

### Ejecución

1. **Clona el repositorio:**
   ```bash
   git clone [https://github.com/OpiumnJei/images-api.git](https://github.com/OpiumnJei/images-api.git)
   cd images-api
   ```

2. **Levanta los contenedores:**
   Desde la raíz del proyecto (donde se encuentra el archivo `docker-compose.yml`), ejecuta el siguiente comando. La
   primera vez, Docker construirá la imagen de la API, lo cual puede tardar unos minutos.

   ```bash
   docker-compose up --build
   ```

¡Y listo! La aplicación se encargará de todo: construirá la imagen, iniciará la base de datos, aplicará las migraciones
con Flyway y finalmente iniciará la API.

- **API disponible en:** `http://localhost:8080`
- **Base de Datos (MySQL) expuesta en:** `localhost` en el puerto `3307`

---

## 📖 Documentación y Endpoints de la API

Una vez que la aplicación esté corriendo, puedes explorar todos los endpoints de forma interactiva a través de la
documentación de Swagger UI.

➡️ **Accede a la documentación
aquí: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

La documentación te permitirá ver todos los endpoints públicos y de administrador, sus parámetros, y probarlos
directamente desde el navegador. Para los endpoints de administrador, primero deberás obtener un token JWT a través del
endpoint de login y luego usarlo en el botón "Authorize".

---

## 📊 Monitoreo y Observabilidad

La API expone endpoints de **Spring Boot Actuator** para monitorear el estado de los patrones de resiliencia en tiempo
real:

| Endpoint                    | Descripción                                  |
|:----------------------------|:---------------------------------------------|
| `/actuator/health`          | Estado general de la aplicación              |
| `/actuator/circuitbreakers` | Estado de los Circuit Breakers (OPEN/CLOSED) |
| `/actuator/ratelimiters`    | Métricas de los Rate Limiters                |
| `/actuator/retries`         | Estadísticas de reintentos                   |

---

## 🧪 Tests de Carga (k6)

La carpeta `k6-tests/` contiene scripts para probar los patrones de resiliencia:

```bash
# Test de humo (verificar que funciona)
k6 run k6-tests/test-smoke.js

# Test de Rate Limiter
k6 run k6-tests/test-rate-limiter.js

# Test de Circuit Breaker (apagar BD para simular fallo)
k6 run k6-tests/test-circuit-breaker.js

# Test combinado de carga
k6 run k6-tests/test-load-combined.js
```

➡️ **Documentación completa de k6:** Ver `k6-tests/README.md`

---

## 📋 Documentación Adicional

| Documento                                | Descripción                      |
|:-----------------------------------------|:---------------------------------|
| [CHANGELOG.md](CHANGELOG.md)             | Historial de cambios y versiones |
| [DEPLOYMENT.md](DEPLOYMENT.md)           | Guía de despliegue a producción  |
| [k6-tests/README.md](k6-tests/README.md) | Documentación de tests de carga  |

---

## 📜 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.