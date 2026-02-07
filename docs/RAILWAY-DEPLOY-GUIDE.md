# 🚀 Guía de Despliegue en Railway - Images API

## 📁 Archivos Importantes

### ✅ Archivos que SÍ se suben a GitHub (y Railway los usa)
| Archivo | Propósito |
|---------|-----------|
| `Dockerfile` | Instrucciones de construcción del contenedor |
| `application.properties` | Configuración base (usa variables de entorno) |
| `application-prod.properties` | Configuración de producción |
| `application-dev.properties` | Configuración de desarrollo |
| `.env.example` | Plantilla de referencia para otros desarrolladores |
| `railway.json` | Configuración específica de Railway |

### ❌ Archivos que NUNCA se suben a GitHub
| Archivo | Razón |
|---------|-------|
| `.env` | Contiene credenciales reales (tu contraseña de MySQL, JWT secret) |

---

## 🔐 Variables de Entorno

### 🏠 VARIABLES PARA LOCAL (tu archivo `.env`)

```bash
# BASE DE DATOS LOCAL
DATASOURCE_URL=jdbc:mysql://localhost:3306/images_app
DATASOURCE_USERNAME=root
DATASOURCE_PASSWORD=tu_password_local

# JWT
API_SECURITY_TOKEN_SECRET=tu-clave-secreta-local

# PERFIL
SPRING_PROFILES_ACTIVE=dev

# PUERTO
PORT=8080
```

---

### ☁️ VARIABLES PARA RAILWAY (configurar en el Dashboard)

En Railway, debes configurar estas variables en **Settings > Variables**:

| Variable | Valor |
|----------|-------|
| `DATASOURCE_URL` | `jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}` |
| `DATASOURCE_USERNAME` | `${{MySQL.MYSQL_USER}}` |
| `DATASOURCE_PASSWORD` | `${{MySQL.MYSQL_PASSWORD}}` |
| `API_SECURITY_TOKEN_SECRET` | `una-clave-aleatoria-segura-de-minimo-32-caracteres` |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `CORS_ALLOWED_ORIGINS` | `https://tu-app.railway.app` (opcional) |

> **Nota**: No configures `PORT`, Railway lo asigna automáticamente.

---

## 🗄️ ¿Cómo se Comunica MySQL en Railway?

### Paso 1: Agregar MySQL a tu proyecto
1. En Railway Dashboard → Tu proyecto → **+ New** → **Database** → **MySQL**
2. Railway crea automáticamente un servicio MySQL

### Paso 2: Variables Automáticas de Railway
Cuando agregas MySQL, Railway crea estas variables:

| Variable Railway | Ejemplo de Valor |
|------------------|------------------|
| `MYSQL_HOST` | `roundhouse.proxy.rlwy.net` |
| `MYSQL_PORT` | `26841` |
| `MYSQL_DATABASE` | `railway` |
| `MYSQL_USER` | `root` |
| `MYSQL_PASSWORD` | `aBcDeFgH123456` |

### Paso 3: Referenciar Variables (Sintaxis Railway)
```bash
# Sintaxis: ${{NombreServicio.VARIABLE}}
DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}
```

Railway sustituye automáticamente `${{MySQL.MYSQL_HOST}}` por el valor real en tiempo de ejecución.

---

## 📋 Configuración Paso a Paso en Railway

### 1. Crear Proyecto
1. Ve a [railway.app](https://railway.app)
2. Click en **New Project**
3. Selecciona **Deploy from GitHub repo**
4. Autoriza y selecciona tu repositorio

### 2. Agregar Base de Datos
1. En el proyecto → **+ New** → **Database** → **Add MySQL**
2. Railway creará el servicio MySQL automáticamente

### 3. Configurar Variables de Entorno
1. Click en tu servicio de API
2. Ve a **Variables**
3. Agrega cada variable:

```
DATASOURCE_URL = jdbc:mysql://${{MySQL.MYSQL_HOST}}:${{MySQL.MYSQL_PORT}}/${{MySQL.MYSQL_DATABASE}}
DATASOURCE_USERNAME = ${{MySQL.MYSQL_USER}}
DATASOURCE_PASSWORD = ${{MySQL.MYSQL_PASSWORD}}
API_SECURITY_TOKEN_SECRET = [genera-una-clave-segura]
SPRING_PROFILES_ACTIVE = prod
```

### 4. Verificar Health Check
Railway usará `/actuator/health` automáticamente (configurado en `railway.json`).

---

## 🔄 Flujo de Configuración

```
┌─────────────────────────────────────────────────────────────┐
│                     DESARROLLO LOCAL                         │
├─────────────────────────────────────────────────────────────┤
│  Archivo: .env (NO se sube a git)                           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ DATASOURCE_URL=jdbc:mysql://localhost:3306/images_app    │
│  │ DATASOURCE_USERNAME=root                             │    │
│  │ DATASOURCE_PASSWORD=tu_password_local                │    │
│  │ SPRING_PROFILES_ACTIVE=dev                           │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                             │
│  Archivos cargados:                                         │
│  • application.properties (base)                            │
│  • application-dev.properties (perfil activo)               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     RAILWAY (PRODUCCIÓN)                     │
├─────────────────────────────────────────────────────────────┤
│  Variables: Dashboard → Settings → Variables                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQL_HOST}}:..│    │
│  │ DATASOURCE_USERNAME=${{MySQL.MYSQL_USER}}           │    │
│  │ DATASOURCE_PASSWORD=${{MySQL.MYSQL_PASSWORD}}       │    │
│  │ SPRING_PROFILES_ACTIVE=prod                         │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                             │
│  Archivos cargados:                                         │
│  • application.properties (base)                            │
│  • application-prod.properties (perfil activo)              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛡️ Seguridad

### Generar clave JWT segura (PowerShell)
```powershell
[System.Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

### Generar clave JWT segura (Linux/Mac)
```bash
openssl rand -hex 32
```

### Reglas de Seguridad
1. **NUNCA** commitear el archivo `.env`
2. **SIEMPRE** usar claves diferentes para local y producción
3. **SIEMPRE** usar claves JWT de mínimo 256 bits (32 caracteres)

---

## 🎯 Resumen

| Concepto | Local | Railway |
|----------|-------|---------|
| Archivo de secretos | `.env` | Dashboard → Variables |
| Perfil Spring | `dev` | `prod` |
| MySQL Host | `localhost` | `${{MySQL.MYSQL_HOST}}` |
| MySQL Port | `3306` | `${{MySQL.MYSQL_PORT}}` |
| Puerto App | `8080` | Automático |
| JWT Secret | Puede ser simple | **DEBE ser seguro** |
| Health Check | No necesario | `/actuator/health` |
