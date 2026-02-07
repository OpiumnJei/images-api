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

## 🏗️ Opciones de Hosting

### 🥇 **Render + TiDB Cloud** - RECOMENDADO (100% GRATIS)

**¿Por qué esta combinación?**

- ✅ **Costo: $0/mes** - Ambos servicios tienen tier gratuito
- ✅ **MySQL compatible** - TiDB es 100% compatible con MySQL
- ✅ **SSL incluido** - Conexiones seguras sin configuración extra
- ✅ **Deploy automático** - Desde GitHub
- ✅ **Sin límite de horas** - 750 hrs/mes en Render (suficiente)

**⚠️ Limitaciones del tier gratuito:**
- Render: App se "duerme" tras 15 min de inactividad (cold start ~30s)
- Render: 512MB RAM (optimizado en `application-render.properties`)
- TiDB: 5GB almacenamiento, 25GB Request Units/mes

**📚 Guía completa:** [`docs/RENDER-TIDB-DEPLOY-GUIDE.md`](docs/RENDER-TIDB-DEPLOY-GUIDE.md)

---

### 🥈 **Railway.app** - Alternativa de pago

**¿Por qué Railway?**

- Perfecto si necesitas más recursos
- MySQL incluido en el mismo proveedor
- Sin sleep (siempre activo)
- Interfaz muy amigable

**💰 Costo: ~$5-10/mes**

- $5 crédito gratis al registrarte (prueba inicial)
- API: ~$5/mes
- MySQL: ~$5/mes

**📚 Guía completa:** [`docs/RAILWAY-DEPLOY-GUIDE.md`](docs/RAILWAY-DEPLOY-GUIDE.md)

---

### 🥉 **VPS (DigitalOcean, Hetzner, etc.)** - Para más control

**¿Cuándo elegir VPS?**

- Necesitas control total del servidor
- Múltiples aplicaciones en un solo servidor
- Costos predecibles a largo plazo

**💰 Costo: $4-6/mes** (Droplet básico)

| Proveedor | Plan Mínimo | RAM | Almacenamiento |
|-----------|-------------|-----|----------------|
| DigitalOcean | $4/mes | 512MB | 10GB SSD |
| Hetzner | €3.79/mes | 2GB | 20GB SSD |
| Vultr | $5/mes | 1GB | 25GB SSD |

> ⚠️ Requiere conocimientos de Linux, Docker, y administración de servidores.

---

## 📊 Comparativa Rápida

| Servicio | Costo/mes | MySQL | Sleep | Dificultad | Ideal para |
|----------|-----------|-------|-------|------------|------------|
| **Render + TiDB** | **$0** | ✅ (TiDB) | Sí | Fácil | 🏆 Proyectos nuevos/POC |
| **Railway** | $10 | ✅ | No | Fácil | Apps en producción activa |
| **VPS** | $4-6 | ✅ | No | Alta | Control total |

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
- [ ] Configurar variables en tu hosting elegido
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
   
4. En tu hosting elegido:
   - Conectar repo GitHub
   - Configurar variables de entorno
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

1. **Empieza con Render + TiDB** - Es gratis y suficiente para validar tu proyecto
2. **Usa UptimeRobot** - Gratis para evitar el sleep de Render
3. **Monitorea los logs** - Render los muestra en tiempo real
4. **Migra a Railway/VPS** cuando generes ingresos - Escalar es fácil

---

**🎉 ¡Tu API está lista para producción!**

La arquitectura que tienes (resiliencia, seguridad, Docker) es sólida para un proyecto real.

---

## 📦 Archivos de Configuración por Plataforma

| Archivo | Plataforma | Descripción |
|---------|------------|-------------|
| `render.yaml` | Render | Configuración de infraestructura |
| `application-render.properties` | Render | Perfil optimizado para 512MB |
| `railway.json` | Railway | Configuración de build y deploy |
| `Dockerfile` | Todas | Build multi-stage optimizado |
| `docker-compose.yml` | Local/VPS | Orquestación con MySQL |

---

## 🔒 Generar JWT Secret Seguro

Para producción, genera una clave segura:

**Opción PowerShell:**
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

**Opción Bash/Linux:**
```bash
openssl rand -base64 32
```

**Opción Online:**
- https://generate-secret.vercel.app/32

---

## 📊 Monitoreo en Producción

Una vez desplegado, estos endpoints estarán disponibles:

| Endpoint | Uso |
|----------|-----|
| `/actuator/health` | Verificar estado general |
| `/actuator/health/liveness` | Liveness probe |
| `/actuator/health/readiness` | Readiness probe |
| `/actuator/circuitbreakers` | Estado de Circuit Breakers |
| `/actuator/ratelimiters` | Estado de Rate Limiters |
| `/swagger-ui/index.html` | Documentación interactiva |

---

## 📚 Guías Detalladas

| Guía | Descripción |
|------|-------------|
| [`docs/RENDER-TIDB-DEPLOY-GUIDE.md`](docs/RENDER-TIDB-DEPLOY-GUIDE.md) | 🏆 Deploy gratuito en Render + TiDB Cloud |
| [`docs/RAILWAY-DEPLOY-GUIDE.md`](docs/RAILWAY-DEPLOY-GUIDE.md) | Deploy de pago en Railway |
| [`docs/HIKARICP-GUIA.md`](docs/HIKARICP-GUIA.md) | Optimización de conexiones a BD |
