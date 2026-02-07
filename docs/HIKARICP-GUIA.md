# 📚 Guía Completa de HikariCP - Pool de Conexiones

## Tabla de Contenidos
1. [¿Qué es HikariCP?](#qué-es-hikaricp)
2. [¿Qué problema resuelve?](#qué-problema-resuelve)
3. [¿Qué es un Pool de Conexiones?](#qué-es-un-pool-de-conexiones)
4. [¿Qué significa "5 conexiones"?](#qué-significa-5-conexiones)
5. [Estados de una Conexión](#estados-de-una-conexión)
6. [¿Por qué renovar conexiones?](#por-qué-renovar-conexiones)
7. [Configuración Explicada](#configuración-explicada)

---

## ¿Qué es HikariCP?

**HikariCP** es un **pool de conexiones** (Connection Pool) para bases de datos en Java. Es el pool de conexiones **por defecto en Spring Boot** debido a su rendimiento extremadamente rápido y su bajo consumo de recursos.

"Hikari" significa "luz" en japonés, reflejando su filosofía de ser ligero y rápido.

---

## ¿Qué problema resuelve?

Imagina que tu API necesita consultar la base de datos. Sin un pool de conexiones:

```
Usuario 1 hace petición → Abre conexión a BD → Consulta → Cierra conexión ❌
Usuario 2 hace petición → Abre conexión a BD → Consulta → Cierra conexión ❌
Usuario 3 hace petición → Abre conexión a BD → Consulta → Cierra conexión ❌
```

**Problema:** Abrir y cerrar conexiones es **MUY costoso** (toma ~200-500ms cada vez). Es como si cada vez que quisieras entrar a tu casa, tuvieras que construir la puerta, entrar, y luego destruirla.

---

## ¿Qué es un Pool de Conexiones?

Con HikariCP:

```
Al iniciar la app:
   HikariCP crea 5 conexiones "listas" y las mantiene abiertas ✅

Usuario 1 hace petición → Toma conexión del pool → Consulta → Devuelve al pool
Usuario 2 hace petición → Toma conexión del pool → Consulta → Devuelve al pool
Usuario 3 hace petición → Toma conexión del pool → Consulta → Devuelve al pool
```

**Analogía:** Es como tener 5 llaves de tu casa siempre disponibles. Cuando alguien necesita entrar, toma una llave, entra, y cuando sale, devuelve la llave para que otro la use.

### Visualización del Pool

```
                         HikariCP Pool
                    ┌─────────────────────┐
                    │  ┌───┐ ┌───┐ ┌───┐  │
 Tu API  ──────────►│  │ 1 │ │ 2 │ │ 3 │  │──────► MySQL
 (múltiples         │  └───┘ └───┘ └───┘  │       (Railway)
  usuarios)         │  ┌───┐ ┌───┐        │
                    │  │ 4 │ │ 5 │        │
                    │  └───┘ └───┘        │
                    └─────────────────────┘
                         5 conexiones
                         reutilizables
```

---

## ¿Qué significa "5 conexiones"?

```yaml
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
```

| Parámetro | Valor | Significado |
|-----------|-------|-------------|
| `maximum-pool-size` | 5 | Máximo 5 conexiones simultáneas a la BD |
| `minimum-idle` | 2 | Siempre mantener al menos 2 conexiones listas |

**¿Por qué 5?** 

Railway (tier gratuito/starter) tiene límites en las conexiones de MySQL. Con 5 conexiones:
- Evitas sobrecargar la BD
- Suficiente para manejar tráfico moderado
- Si tienes 10 usuarios simultáneos, 5 esperan mientras los otros 5 usan las conexiones

### Comparación de Rendimiento

| Sin Pool | Con HikariCP |
|----------|--------------|
| ~300ms por conexión nueva | ~1ms (ya está lista) |
| Agota recursos del servidor | Recursos controlados |
| La BD se sobrecarga | Límite de conexiones |
| Errores bajo carga | Estable bajo carga |

---

## Estados de una Conexión

### Conexión "Disponible"

Una conexión **disponible** es una conexión que está en el pool, lista para ser usada, pero que **ningún usuario está usando en este momento**.

```
Pool de HikariCP (5 conexiones máx)
┌────────────────────────────────────────────────────┐
│                                                    │
│  [Conexión 1] ← Usuario A consultando (EN USO)    │
│  [Conexión 2] ← Usuario B consultando (EN USO)    │
│  [Conexión 3] ← Esperando... (DISPONIBLE) ✅      │
│  [Conexión 4] ← Esperando... (DISPONIBLE) ✅      │
│  [Conexión 5] ← Esperando... (DISPONIBLE) ✅      │
│                                                    │
└────────────────────────────────────────────────────┘
```

**`connection-timeout=30000`** significa:
> "Si un usuario necesita una conexión pero TODAS están en uso, esperará máximo 30 segundos. Si no se libera ninguna, lanza error."

### Conexión "Inactiva" (Idle)

Una conexión **inactiva** es una conexión que está abierta pero que **nadie ha usado por un tiempo**.

```
Escenario: Es de madrugada, no hay usuarios

Pool de HikariCP
┌────────────────────────────────────────────────────┐
│                                                    │
│  [Conexión 1] ← Sin usar hace 2 min (INACTIVA)    │
│  [Conexión 2] ← Sin usar hace 5 min (INACTIVA)    │
│  [Conexión 3] ← Sin usar hace 8 min (INACTIVA)    │
│  [Conexión 4] ← Sin usar hace 10 min (INACTIVA)   │ ← ¡Se cierra!
│  [Conexión 5] ← Sin usar hace 12 min (CERRADA)    │
│                                                    │
└────────────────────────────────────────────────────┘
```

**`idle-timeout=600000`** (10 minutos) significa:
> "Si una conexión lleva más de 10 minutos sin usarse, ciérrala para liberar recursos."

**¿Por qué cerrar conexiones inactivas?**
- Cada conexión consume memoria en tu servidor Y en MySQL
- Si no hay tráfico, no necesitas 5 conexiones abiertas
- El `minimum-idle=2` garantiza que siempre queden al menos 2 listas

---

## ¿Por qué renovar conexiones?

Las conexiones **deben renovarse periódicamente** (`max-lifetime`) por varias razones:

### 1. Conexiones "zombies" o corruptas

```
Conexión creada hace 3 horas
        │
        ▼
   [Problemas posibles]
   - La red tuvo un micro-corte
   - MySQL reinició silenciosamente
   - Firewall cerró la conexión por inactividad
   - Memory leak acumulado
        │
        ▼
   La conexión PARECE viva, pero está MUERTA 💀
```

### 2. Límites del servidor de BD

MySQL y otros servidores tienen configuraciones como:
```
wait_timeout = 28800  (8 horas en MySQL por defecto)
```
> "Si una conexión no hace nada por 8 horas, MySQL la cierra automáticamente"

Si HikariCP no sabe esto, intentará usar una conexión que MySQL ya cerró → **ERROR**.

### 3. Balanceo de carga

En entornos cloud (como Railway), la BD puede tener múltiples nodos. Renovar conexiones permite redistribuir la carga.

### Ciclo de Vida Completo

```
                    CICLO DE VIDA DE UNA CONEXIÓN
                    
Tiempo 0:00        Se CREA la conexión
    │              ┌─────────────────┐
    │              │  Conexión Nueva │
    │              └────────┬────────┘
    │                       │
    ▼                       ▼
Tiempo 0:05        Usuario la USA
    │              ┌─────────────────┐
    │              │    EN USO       │
    │              └────────┬────────┘
    │                       │
    ▼                       ▼
Tiempo 0:06        Usuario termina, vuelve al pool
    │              ┌─────────────────┐
    │              │   DISPONIBLE    │ ← idle-timeout empieza a contar
    │              └────────┬────────┘
    │                       │
    ▼                       ▼
Tiempo 0:16        10 min sin usarse
    │              ┌─────────────────┐
    │              │    INACTIVA     │ ← Si hay más de minimum-idle, se CIERRA
    │              └────────┬────────┘
    │                       │
    ▼                       ▼
Tiempo 0:30        Cumple 30 min de vida (max-lifetime)
                   ┌─────────────────┐
                   │    RENOVADA     │ ← Se cierra y crea una NUEVA
                   └─────────────────┘
```

---

## Configuración Explicada

### Tu configuración en `application-prod.properties`:

```properties
# =====================================================
# HIKARICP - POOL DE CONEXIONES BD
# =====================================================

# Máximo 5 conexiones simultáneas (ajustado para Railway)
spring.datasource.hikari.maximum-pool-size=5

# Siempre tener al menos 2 conexiones listas
spring.datasource.hikari.minimum-idle=2

# Si necesitas una conexión pero todas están ocupadas, espera máximo 30 seg
spring.datasource.hikari.connection-timeout=30000

# Si una conexión lleva 10 min sin usarse, ciérrala (ahorra recursos)
spring.datasource.hikari.idle-timeout=600000

# Aunque se use constantemente, a los 30 min ciérrala y crea una nueva (evita zombies)
spring.datasource.hikari.max-lifetime=1800000

# Si una conexión tarda más de 1 min en devolverse, ALERTA (posible bug en tu código)
spring.datasource.hikari.leak-detection-threshold=60000
```

### Tabla Resumen de Parámetros

| Parámetro | Valor | Pregunta que responde |
|-----------|-------|----------------------|
| `maximum-pool-size` | 5 | "¿Cuántas conexiones simultáneas máximo?" |
| `minimum-idle` | 2 | "¿Cuántas conexiones mantener siempre listas?" |
| `connection-timeout` | 30s | "¿Cuánto espero si no hay conexiones libres?" |
| `idle-timeout` | 10min | "¿Cuánto puede estar inactiva antes de cerrarla?" |
| `max-lifetime` | 30min | "¿Cuál es la vida máxima de una conexión?" |
| `leak-detection-threshold` | 1min | "¿Cuándo alertar de posible fuga de conexiones?" |

---

## Recomendaciones por Entorno

| Entorno | max-pool-size | minimum-idle | Notas |
|---------|---------------|--------------|-------|
| **Desarrollo** | 2-5 | 1 | Pocos recursos necesarios |
| **Railway Free** | 5 | 2 | Límite de ~10 conexiones en BD |
| **Railway Pro** | 10-20 | 5 | Más capacidad |
| **Producción Alta Carga** | 20-50 | 10 | Según RAM y BD |

---

## Conclusión

**HikariCP es el "portero" que administra eficientemente quién puede hablar con tu base de datos y cuándo**, evitando que se sature. 

Con la configuración de 5 conexiones:
- Tu API puede atender hasta 5 consultas a la BD simultáneamente
- Las demás peticiones esperan su turno (máx 30 segundos)
- Las conexiones se renuevan cada 30 minutos para evitar problemas
- Las conexiones inactivas se cierran después de 10 minutos para ahorrar recursos

Esta configuración es óptima para un despliegue en Railway con tráfico moderado.
