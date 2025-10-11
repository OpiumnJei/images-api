# API de Saludos con Imágenes (images-api)

![Java](https://img.shields.io/badge/Java-17-blue?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=spring)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange?style=for-the-badge&logo=mysql)

API RESTful construida con **Spring Boot** y totalmente contenerizada con **Docker**. Diseñada para ser el backend de
una aplicación móvil de saludos, permitiendo la gestión completa de imágenes, categorías y temáticas.

---

## 🚀 Características Principales

* **🔐 Autenticación y Autorización con JWT:** Endpoints de administrador protegidos usando JSON Web Tokens, con roles
  diferenciados (`ADMIN`, `CLIENT`).
* **CRUD Completo:** Operaciones para crear, leer, actualizar y eliminar categorías, temáticas e imágenes.
* **paginación:** La obtención de listas de imágenes está paginada para un rendimiento eficiente.
* **Arquitectura en capas:** Clara separación de responsabilidades (Controladores, Servicios, Repositorios).
* **Documentación con Swagger (OpenAPI):** Documentación de la API generada automáticamente y accesible de forma
  interactiva.
* **Entorno Dockerizado:** La aplicación y su base de datos se levantan con un solo comando gracias a `docker-compose`.

---

## 🛠️ Stack Tecnológico

| Backend         | Base de Datos | Contenerización | Testing |
|:----------------|:--------------|:----------------|:--------|
| Java 17         | MySQL 8.0     | Docker          | JUnit 5 |
| Spring Boot 3   | Flyway        | Docker Compose  | Mockito |
| Spring Security |               |                 |         |
| Maven           |               |                 |         |

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

## 📜 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.