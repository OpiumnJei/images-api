# 🚀 Guía de Despliegue: Render + TiDB Cloud (100% GRATIS)

## 📋 Arquitectura

```
┌─────────────────────┐         ┌─────────────────────┐
│   RENDER (FREE)     │  SSL    │  TiDB Cloud (FREE)  │
│   ┌─────────────┐   │ ──────► │   ┌─────────────┐   │
│   │  images-api │   │         │   │    MySQL    │   │
│   │   512MB     │   │         │   │     5GB     │   │
│   └─────────────┘   │         │   └─────────────┘   │
│                     │         │                     │
│  • Sleep: 15 min    │         │  • Sin sleep        │
│  • 750 hrs/mes      │         │  • 25GB RUs/mes     │
└─────────────────────┘         └─────────────────────┘
```

**Costo total: $0/mes** ✅

---

## 📝 Paso 1: Crear Base de Datos en TiDB Cloud

### 1.1 Registrarse en TiDB Cloud

1. Ir a [https://tidbcloud.com](https://tidbcloud.com)
2. Registrarse con GitHub o Google
3. Seleccionar **"TiDB Serverless"** (es el gratuito)

### 1.2 Crear Cluster

1. Click en **"Create Cluster"**
2. Seleccionar **"Serverless"**
3. Elegir región: **`us-east-1`** (o la más cercana a tu audiencia)
4. Nombre del cluster: `images-api-db`
5. Click **"Create"**

### 1.3 Configurar Base de Datos

1. Una vez creado, ir a **"Connect"**
2. En **"Connect With"** seleccionar **"Java (MySQL Connector)"**
3. Copiar los datos de conexión:

```
Host:     gateway01.us-east-1.prod.aws.tidbcloud.com
Port:     4000
User:     xxxxx.root
Password: (generado automáticamente)
Database: test (cambiar a images_app)
```

### 1.4 Crear la Base de Datos

1. En TiDB Cloud, ir a **"SQL Editor"**
2. Ejecutar:

```sql
CREATE DATABASE IF NOT EXISTS images_app;
```

### 1.5 Obtener URL de Conexión

La URL JDBC tendrá este formato:

```
jdbc:mysql://gateway01.us-east-1.prod.aws.tidbcloud.com:4000/images_app?sslMode=VERIFY_IDENTITY&sslCA=/etc/ssl/certs/ca-certificates.crt
```

> ⚠️ **Importante:** TiDB Cloud requiere SSL. La configuración `sslMode=VERIFY_IDENTITY` es obligatoria.

---

## 📝 Paso 2: Desplegar API en Render

### 2.1 Registrarse en Render

1. Ir a [https://render.com](https://render.com)
2. Registrarse con GitHub

### 2.2 Crear Web Service

1. Click en **"New +"** → **"Web Service"**
2. Conectar tu repositorio de GitHub
3. Seleccionar el repo `images-api`

### 2.3 Configuración del Servicio

| Campo | Valor | Notas |
|-------|-------|-------|
| **Name** | `images-api` | Nombre único para tu servicio |
| **Language** | `Docker` | Render usará el Dockerfile |
| **Branch** | `main` | Rama de producción |
| **Region** | `Virginia (US East)` | ⚠️ Debe coincidir con TiDB (`us-east-1`) |
| **Root Directory** | *(dejar vacío)* | El Dockerfile raíz ya maneja la estructura |

> 💡 **Tip:** Si aún no has mergeado a `main`, puedes usar temporalmente `release/v0.0.1` y cambiarlo después.

### 2.4 Variables de Entorno

En la sección **"Environment Variables"**, agregar:

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `render` |
| `DATASOURCE_URL` | `jdbc:mysql://gateway01.us-east-1.prod.aws.tidbcloud.com:4000/images_app?sslMode=VERIFY_IDENTITY` |
| `DATASOURCE_USERNAME` | `tu_usuario.root` (de TiDB Cloud) |
| `DATASOURCE_PASSWORD` | `tu_password` (de TiDB Cloud) |
| `API_SECURITY_TOKEN_SECRET` | (generar con: `openssl rand -base64 32`) |
| `CORS_ALLOWED_ORIGINS` | `*` (o tu dominio frontend) |

### 2.5 Deploy

1. Click en **"Create Web Service"**
2. Esperar el build (~3-5 minutos)
3. Render te dará una URL: `https://images-api-xxxx.onrender.com`

---

## ✅ Paso 3: Verificar Despliegue

### 3.1 Health Check

```bash
curl https://images-api-xxxx.onrender.com/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### 3.2 Probar API

```bash
# Swagger UI
https://images-api-xxxx.onrender.com/swagger-ui.html

# Endpoint de ejemplo
curl https://images-api-xxxx.onrender.com/api/categories
```

---

## ⚠️ Consideraciones Importantes

### Sleep de Render (15 minutos)

El tier gratuito de Render "duerme" la app después de 15 minutos de inactividad.

**Soluciones:**

1. **Aceptar el cold start** (~30 segundos para despertar)

2. **Usar UptimeRobot (gratis)** para hacer ping cada 14 minutos:
   - Ir a [https://uptimerobot.com](https://uptimerobot.com)
   - Crear monitor HTTP
   - URL: `https://images-api-xxxx.onrender.com/actuator/health`
   - Intervalo: 5 minutos

3. **GitHub Actions cron job** (ping automático):

```yaml
# .github/workflows/keep-alive.yml
name: Keep Render Alive
on:
  schedule:
    - cron: '*/14 * * * *'  # Cada 14 minutos
jobs:
  ping:
    runs-on: ubuntu-latest
    steps:
      - name: Ping API
        run: curl -s https://images-api-xxxx.onrender.com/actuator/health
```

### Límites de TiDB Cloud Serverless

| Recurso | Límite Gratuito |
|---------|-----------------|
| Request Units | 25 GB/mes |
| Almacenamiento | 5 GB |
| Conexiones | Sin límite práctico |

Para una API pequeña/mediana, estos límites son **más que suficientes**.

---

## 🔧 Troubleshooting

### Error: "Communications link failure"

**Causa:** SSL no configurado correctamente.

**Solución:** Asegúrate de que la URL incluya:
```
?sslMode=VERIFY_IDENTITY
```

### Error: "Access denied"

**Causa:** Credenciales incorrectas.

**Solución:** 
1. Verificar usuario/password en TiDB Cloud → Connect
2. El usuario suele ser `cluster_name.root`

### Error: "Too many connections"

**Causa:** Pool de conexiones muy grande.

**Solución:** Ya configurado en `application-render.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=3
```

### App muy lenta al despertar

**Causa:** Cold start de Render + conexión a TiDB.

**Solución:** Usar UptimeRobot o GitHub Actions para mantener activa.

---

## 📊 Métricas de Uso

### Monitorear en Render
- Dashboard → Logs (ver errores)
- Dashboard → Metrics (CPU/RAM)

### Monitorear en TiDB Cloud
- Cluster → Monitoring → Request Units
- Cluster → Monitoring → Connections

---

## 🎯 Resumen

| Servicio | Función | Costo |
|----------|---------|-------|
| Render | API Spring Boot | $0 |
| TiDB Cloud | MySQL Database | $0 |
| UptimeRobot | Keep-alive ping | $0 |
| **Total** | | **$0/mes** |

✅ **Tu API está lista para producción sin gastar un centavo.**
