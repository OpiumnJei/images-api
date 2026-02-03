# 🚀 Guía de Despliegue a Producción - Images API v0.0.1

## 📋 Evaluación: ¿Está lista para producción?

### ✅ SÍ - Tu API cumple con los requisitos para producción

| Categoría         | Estado | Descripción                                        |
|-------------------|--------|----------------------------------------------------|
| **Resiliencia**   | ✅      | Circuit Breaker, Retry, Rate Limiter implementados |
| **Seguridad**     | ✅      | JWT, roles, CORS configurable                      |
| **Base de Datos** | ✅      | Migraciones Flyway, HikariCP optimizado            |
| **Contenedores**  | ✅      | Docker & Docker Compose listos                     |
| **Monitoreo**     | ✅      | Actuator con health checks                         |
| **Testing**       | ✅      | Tests unitarios + k6 para carga                    |
| **Documentación** | ✅      | Swagger, README, CHANGELOG                         |

---

## 🏗️ Hosting Recomendado (Económico)

### 🥇 **Railway.app** - RECOMENDADO PARA TI

**¿Por qué Railway?**

- Perfecto para primer deploy
- Interfaz muy amigable
- Deploy automático desde GitHub
- MySQL incluido
- SSL gratis

**💰 Costo: ~$5-10/mes**

- $5 crédito gratis al registrarte
- API: ~$5/mes
- MySQL: ~$5/mes

### Pasos para Deploy en Railway:

```
1. Ir a https://railway.app
2. Registrarte con GitHub
3. "New Project" → "Deploy from GitHub repo"
4. Seleccionar tu repo: images-api
5. Railway detectará el Dockerfile automáticamente
```

**Agregar MySQL:**

```
1. En tu proyecto Railway → "+ New"
2. Database → MySQL
3. Railway genera las credenciales automáticamente
```

**Variables de entorno en Railway:**

```env
SPRING_PROFILES_ACTIVE=prod
DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}
DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}
DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}
API_SECURITY_TOKEN_SECRET=tu-jwt-secret-muy-seguro-de-256-bits
CORS_ALLOWED_ORIGINS=https://tu-app.railway.app
```

---

### 🥈 **Render.com** - Alternativa gratuita

**💰 Costo: GRATIS (con limitaciones)**

- La app se "duerme" tras 15 min de inactividad
- Solo PostgreSQL (no MySQL)

**Si quieres probar gratis:** Render es buena opción inicial.

---

### 🥉 **Fly.io** - Alternativa técnica

**💰 Costo: $5-10/mes**

- Más técnico (requiere CLI)
- Buen rendimiento global

---

## 📊 Comparativa Rápida

| Servicio    | Costo/mes | MySQL | Sleep | Dificultad | Recomendado |
|-------------|-----------|-------|-------|------------|-------------|
| **Railway** | $10       | ✅     | No    | Fácil      | ⭐⭐⭐         |
| **Render**  | $0-7      | ❌     | Sí    | Fácil      | ⭐⭐          |
| **Fly.io**  | $5-10     | ✅     | No    | Media      | ⭐⭐          |

---

## 🔒 Checklist Pre-Deploy

- [x] `.gitignore` actualizado con `.env`
- [x] `.env.example` creado como plantilla
- [x] `CHANGELOG.md` documentando v0.0.1
- [x] Versión actualizada en `pom.xml` (0.0.1)
- [x] Configuración CORS implementada
- [x] HikariCP optimizado para producción
- [x] Health checks habilitados
- [ ] Crear archivo `.env` local (NO commitear)
- [ ] Configurar variables en Railway
- [ ] Verificar que todo funcione localmente con Docker

---

## 🏷️ Versionado y Releases

### Dónde colocar el Changelog

**Respuesta: AMBOS lugares**

1. **`CHANGELOG.md` en el repo** (ya creado)
    - Historial técnico detallado
    - Para desarrolladores

2. **GitHub Releases** (crear después del merge)
    - Para usuarios finales
    - Descarga de versiones específicas

### Crear Release en GitHub

Después de mergear `release/v0.0.1` a `main`:

```bash
# Crear tag
git tag -a v0.0.1 -m "Release v0.0.1 - Primera versión estable"
git push origin v0.0.1
```

Luego en GitHub:

1. Ir a "Releases" → "Create new release"
2. Seleccionar tag `v0.0.1`
3. Título: "v0.0.1 - Release Inicial"
4. Copiar contenido del CHANGELOG.md

---

## 🚀 Flujo de Deploy Recomendado

```
1. En tu rama release/v0.0.1:
   - Verificar que todo compile: mvn clean package
   - Probar con Docker localmente
   
2. Merge a main:
   git checkout main
   git merge release/v0.0.1
   git push origin main
   
3. Crear tag y release:
   git tag -a v0.0.1 -m "Release v0.0.1"
   git push origin v0.0.1
   
4. En Railway:
   - Conectar repo
   - Configurar variables
   - Deploy automático al detectar push a main
```

---

## 🆘 Verificación Post-Deploy

Una vez desplegado, verificar:

| Endpoint                    | Descripción                |
|-----------------------------|----------------------------|
| `/actuator/health`          | Estado de la aplicación    |
| `/swagger-ui/index.html`    | Documentación API          |
| `/actuator/circuitbreakers` | Estado Circuit Breakers    |
| `/api/home`                 | Endpoint público de prueba |

---

## 💡 Tips para Primer Deploy

1. **Empieza con Railway** - Es el más amigable
2. **Usa el crédito gratis** - $5 para probar
3. **Monitorea los logs** - Railway los muestra en tiempo real
4. **No te preocupes por escalar** - Railway escala fácil cuando necesites

---

**🎉 ¡Tu API está lista para producción!**

La arquitectura que tienes (resiliencia, seguridad, Docker) es sólida para un proyecto real.

---

## 📦 Archivos de Configuración para Railway

Tu proyecto incluye estos archivos optimizados para Railway:

| Archivo | Descripción |
|---------|-------------|
| `railway.json` | Configuración de build y deploy |
| `images-api/Procfile` | Comando de inicio alternativo |
| `images-api/system.properties` | Versión de Java |
| `images-api/dockerfile` | Build multi-stage optimizado |

---

## 🔧 Configuración Paso a Paso en Railway

### 1. Crear Proyecto
```
1. Ir a https://railway.app
2. Registrarte con GitHub
3. Click en "New Project"
4. Seleccionar "Deploy from GitHub repo"
5. Autorizar acceso y seleccionar: images-api
```

### 2. Agregar Base de Datos MySQL
```
1. En tu proyecto → Click en "+ New"
2. Seleccionar "Database" → "MySQL"
3. Railway crea automáticamente las credenciales
```

### 3. Configurar Variables de Entorno
En tu servicio de la API, ir a "Variables" y agregar:

```env
# Perfil de Spring
SPRING_PROFILES_ACTIVE=prod

# Base de datos (usa referencias a MySQL)
DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}
DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}
DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}

# JWT Secret (genera uno seguro de 32+ caracteres)
API_SECURITY_TOKEN_SECRET=TuClaveSecretaMuySeguraDeAlMenos32Caracteres

# CORS (tu dominio de Railway)
CORS_ALLOWED_ORIGINS=https://tu-app.up.railway.app
```

### 4. Verificar Health Check
Railway detectará automáticamente:
- **Health Check Path:** `/actuator/health`
- **Puerto:** Automático via variable `$PORT`

### 5. Obtener URL Pública
```
1. En "Settings" → "Networking"
2. Click en "Generate Domain"
3. Tu API estará en: https://tu-app.up.railway.app
```

---

## 🔒 Generar JWT Secret Seguro

Para producción, genera una clave segura:

**Opción PowerShell:**
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

**Opción Online:**
- https://generate-secret.vercel.app/32

---

## 📊 Monitoreo en Producción

Una vez desplegado, estos endpoints estarán disponibles:

| Endpoint | Uso |
|----------|-----|
| `/actuator/health` | Verificar estado general |
| `/actuator/health/liveness` | Kubernetes/Railway liveness probe |
| `/actuator/health/readiness` | Kubernetes/Railway readiness probe |
| `/actuator/circuitbreakers` | Estado de Circuit Breakers |
| `/actuator/ratelimiters` | Estado de Rate Limiters |
| `/swagger-ui/index.html` | Documentación interactiva |

