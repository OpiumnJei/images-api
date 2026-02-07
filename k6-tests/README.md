# 🧪 Tests de Resiliencia con k6

Esta carpeta contiene scripts de k6 para probar los 3 patrones de resiliencia de tu API.

---

## 📚 GUÍA RÁPIDA DE K6 (Para Juniors)

"Los patrones de resiliencia (Circuit Breaker, Retry, Rate Limiter) protegen tu API de fallos en cascada. k6 te permite
DEMOSTRAR que funcionan."

### ¿Qué es k6?

k6 es una herramienta para hacer **pruebas de carga y rendimiento** a APIs. Simula múltiples usuarios haciendo
peticiones simultáneas.

### Conceptos clave

| Concepto     | Qué significa                   | Ejemplo                                |
|--------------|---------------------------------|----------------------------------------|
| **VU**       | Virtual User (usuario simulado) | `vus: 50` = 50 usuarios                |
| **Duration** | Tiempo que dura el test         | `duration: '30s'`                      |
| **Check**    | Verificación (como assert)      | `check(r, { 'ok': r.status === 200 })` |
| **Sleep**    | Pausa entre peticiones          | `sleep(0.5)` = 500ms                   |
| **Stages**   | Fases de carga progresiva       | Subir de 10 a 100 usuarios             |

### Tipos de tests

| Test       | Propósito              | Usuarios  |
|------------|------------------------|-----------|
| **Smoke**  | Verificar que funciona | 1-5       |
| **Load**   | Carga normal esperada  | 50-100    |
| **Stress** | Encontrar límites      | 100-500+  |
| **Spike**  | Picos repentinos       | 10→200→10 |

### Métricas importantes

| Métrica             | Qué mide               | Valor ideal |
|---------------------|------------------------|-------------|
| `http_req_duration` | Tiempo de respuesta    | p95 < 500ms |
| `http_req_failed`   | % de errores           | < 1%        |
| `checks`            | Verificaciones pasadas | 100%        |

---

## 📋 Prerrequisitos

1. **k6 instalado** (ya lo tienes ✅)
2. **API corriendo** en `http://localhost:8080`

## 🚀 Orden recomendado de ejecución

Abre PowerShell en esta carpeta (`cd C:\Users\Jerlinson\Desktop\images-api\k6-tests`) y ejecuta:

### 0️⃣ Smoke Test (Verificación rápida)

```powershell
k6 run test-smoke.js
```

**¿Qué hace?** Verifica que todos los endpoints responden antes de hacer pruebas de carga.
**¿Qué esperar?** Todos los checks deberían pasar si la API está corriendo.

---

### 1️⃣ Test de Rate Limiter (authRL - Login endpoint)

```powershell
k6 run test-rate-limiter.js
```

**¿Qué hace?** Envía 25 peticiones/segundo al endpoint de login (límite: 10/s).
**¿Qué esperar?** Verás que después de ~10 requests/segundo, recibirás errores **429 (Too Many Requests)**.

---

### 2️⃣ Test de Circuit Breaker + Retry

```powershell
k6 run test-circuit-breaker.js
```

**¿Qué hace?** Carga progresiva (10→30→50 usuarios) a endpoints de home e images.
**¿Qué esperar?** Bajo condiciones normales, ~100% de éxito. Si la BD tiene problemas, el fallback se activa.

---

### 3️⃣ Test de Carga Combinado (Todos los patrones)

```powershell
k6 run test-load-combined.js
```

**¿Qué hace?** Prueba Rate Limiter + Circuit Breaker + Retry bajo carga alta (hasta 100 usuarios).
**¿Qué esperar?** Un resumen completo del comportamiento de todos los patrones.

---

## 📊 Monitoreo en tiempo real

**IMPORTANTE:** Mientras corren los tests, abre estas URLs en tu navegador para ver el estado:

| Patrón               | URL                                            | Qué muestra                       |
|----------------------|------------------------------------------------|-----------------------------------|
| **Health**           | http://localhost:8080/actuator/health          | Estado general + circuit breakers |
| **Circuit Breakers** | http://localhost:8080/actuator/circuitbreakers | Estado: CLOSED, OPEN, HALF_OPEN   |
| **Rate Limiters**    | http://localhost:8080/actuator/ratelimiters    | Permisos disponibles y métricas   |
| **Retries**          | http://localhost:8080/actuator/retries         | Estadísticas de reintentos        |
| **Métricas**         | http://localhost:8080/actuator/metrics         | Todas las métricas disponibles    |

---

## 📈 Interpretar resultados de k6

| Métrica             | Descripción            | Valor ideal                  |
|---------------------|------------------------|------------------------------|
| `http_req_duration` | Tiempo de respuesta    | p95 < 500ms                  |
| `http_req_failed`   | Porcentaje de errores  | < 10% (excepto Rate Limiter) |
| `iterations`        | Total de peticiones    | Depende del test             |
| `checks`            | Verificaciones pasadas | 100% en smoke test           |

---

## 🔧 Solución de problemas

### El Rate Limiter no bloquea nada

1. Verifica que el perfil de resiliencia está activo:
   ```
   spring.config.import=optional:classpath:application-resilience.yml
   ```
2. Revisa que `@RateLimiter(name = "authRL")` está en el controller

### El Circuit Breaker nunca se abre

Esto es **normal** si la BD está funcionando bien. Para probar que funciona:

1. Detén la base de datos
2. Ejecuta `k6 run test-circuit-breaker.js`
3. Deberías ver fallbacks activándose

### Errores de conexión

Asegúrate de que la API está corriendo en `http://localhost:8080`
