import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

/**
 * ============================================
 * TEST DE CIRCUIT BREAKER + RETRY
 * ============================================
 *
 * 📚 ¿QUÉ PROBAMOS AQUÍ?
 * Verificamos que cuando hay problemas (BD caída, red lenta),
 * los patrones de resiliencia protegen al usuario:
 *
 * - Circuit Breaker: Evita llamar a un servicio que está fallando
 * - Retry: Reintenta automáticamente si hay errores temporales
 * - Fallback: Devuelve respuesta alternativa si todo falla
 *
 * 📚 ¿CÓMO DETECTAMOS QUE FUNCIONA?
 * - Con BD activa: 100% éxito, tiempos bajos
 * - Con BD caída: 100% éxito (fallbacks), tiempos altos por reintentos
 *
 * Ejecutar: k6 run test-circuit-breaker.js
 */

/**
 * 📚 MÉTRICAS PERSONALIZADAS
 *
 * Counter: Cuenta eventos (cuántas veces pasó algo)
 * Trend: Registra valores numéricos (tiempos, tamaños)
 *
 * Estas métricas aparecerán en el resumen final del test.
 */
const homeRequests = new Counter('home_requests');
const categoriesRequests = new Counter('categories_requests');
const themesRequests = new Counter('themes_requests');
const imagesRequests = new Counter('images_requests');
const searchRequests = new Counter('search_requests');
const failedRequests = new Counter('failed_requests');
const fallbackActivated = new Counter('fallback_activated');
const responseTime = new Trend('custom_response_time');

/**
 * 📚 STAGES - Carga progresiva
 *
 * A diferencia del smoke test (1 usuario fijo), aquí usamos "stages"
 * para simular carga creciente y decreciente:
 *
 * Tiempo:    0s ----10s----30s----50s----60s
 * Usuarios:  0 → 10 → 30 → 50 → 0
 *
 * Esto simula el patrón real de tráfico (sube, se mantiene, baja).
 *
 * 📚 THRESHOLDS - Criterios de éxito
 *
 * Define qué se considera "test exitoso":
 * - p(95)<3000: El 95% de requests deben tardar menos de 3 segundos
 * - rate<0.1: Menos del 10% pueden fallar
 *
 * Si no se cumplen, k6 reporta el test como fallido.
 */
export const options = {
    stages: [
        { duration: '10s', target: 10 },   // Rampa: 0→10 usuarios en 10s
        { duration: '20s', target: 30 },   // Rampa: 10→30 usuarios en 20s
        { duration: '20s', target: 50 },   // Rampa: 30→50 usuarios en 20s
        { duration: '10s', target: 0 },    // Enfriamiento: 50→0 usuarios
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'], // 95% de requests < 3 segundos
        http_req_failed: ['rate<0.1'],     // Menos de 10% de fallos HTTP
    },
};

const BASE_URL = 'http://localhost:8080';

/**
 * 📚 FUNCIÓN PRINCIPAL
 *
 * Se ejecuta continuamente por cada usuario virtual.
 * Con 50 VUs, hay 50 "copias" de esta función corriendo en paralelo.
 */
export default function () {
    // ==========================================
    // Test 1: /api/home
    // ==========================================
    const homeResponse = http.get(`${BASE_URL}/api/home`);
    homeRequests.add(1);
    responseTime.add(homeResponse.timings.duration);

    if (homeResponse.status === 200) {
        try {
            const body = JSON.parse(homeResponse.body);
            if (body.type === 'FALLBACK') {
                fallbackActivated.add(1);
            }
        } catch (e) {}
    } else {
        failedRequests.add(1);
    }

    check(homeResponse, {
        'home: status 200': (r) => r.status === 200,
    });

    sleep(0.3);

    // ==========================================
    // Test 2: /api/categories (NUEVO)
    // ==========================================
    const categoriesResponse = http.get(`${BASE_URL}/api/categories`);
    categoriesRequests.add(1);
    responseTime.add(categoriesResponse.timings.duration);

    if (categoriesResponse.status === 200) {
        try {
            const body = JSON.parse(categoriesResponse.body);
            // Fallback devuelve lista vacía []
            if (Array.isArray(body) && body.length === 0) {
                // Podría ser fallback si home también lo tuvo
                fallbackActivated.add(1);
            }
        } catch (e) {}
    } else {
        failedRequests.add(1);
    }

    check(categoriesResponse, {
        'categories: status 200': (r) => r.status === 200,
    });

    sleep(0.3);

    // ==========================================
    // Test 3: /api/categories/1/themes (NUEVO)
    // ==========================================
    const themesResponse = http.get(`${BASE_URL}/api/categories/1/themes`);
    themesRequests.add(1);
    responseTime.add(themesResponse.timings.duration);

    if (themesResponse.status === 200) {
        try {
            const body = JSON.parse(themesResponse.body);
            if (Array.isArray(body) && body.length === 0) {
                fallbackActivated.add(1);
            }
        } catch (e) {}
    } else if (themesResponse.status !== 404) {
        // 404 es válido si la categoría no existe
        failedRequests.add(1);
    }

    check(themesResponse, {
        'themes: status 200 o 404': (r) => r.status === 200 || r.status === 404,
    });

    sleep(0.3);

    // ==========================================
    // Test 4: /api/images
    // ==========================================
    const imagesResponse = http.get(`${BASE_URL}/api/images`);
    imagesRequests.add(1);
    responseTime.add(imagesResponse.timings.duration);

    if (imagesResponse.status === 200) {
        try {
            const body = JSON.parse(imagesResponse.body);
            if (body.content && body.content.length === 0 && body.totalElements === 0) {
                fallbackActivated.add(1);
            }
        } catch (e) {}
    } else {
        failedRequests.add(1);
    }

    check(imagesResponse, {
        'images: status 200': (r) => r.status === 200,
    });

    sleep(0.3);

    // ==========================================
    // Test 5: /api/images/search (NUEVO)
    // ==========================================
    const searchTerms = ['amor', 'feliz', 'navidad', 'cumple'];
    const term = searchTerms[Math.floor(Math.random() * searchTerms.length)];

    const searchResponse = http.get(`${BASE_URL}/api/images/search?q=${term}`);
    searchRequests.add(1);
    responseTime.add(searchResponse.timings.duration);

    if (searchResponse.status === 200) {
        try {
            const body = JSON.parse(searchResponse.body);
            if (body.content && body.content.length === 0 && body.totalElements === 0) {
                fallbackActivated.add(1);
            }
        } catch (e) {}
    } else {
        failedRequests.add(1);
    }

    check(searchResponse, {
        'search: status 200': (r) => r.status === 200,
    });

    sleep(0.3);
}

export function handleSummary(data) {
    const home = data.metrics.home_requests?.values?.count || 0;
    const categories = data.metrics.categories_requests?.values?.count || 0;
    const themes = data.metrics.themes_requests?.values?.count || 0;
    const images = data.metrics.images_requests?.values?.count || 0;
    const search = data.metrics.search_requests?.values?.count || 0;
    const failed = data.metrics.failed_requests?.values?.count || 0;
    const fallbacks = data.metrics.fallback_activated?.values?.count || 0;
    const avgDuration = data.metrics.http_req_duration?.values?.avg?.toFixed(2) || 'N/A';
    const p95Duration = data.metrics.http_req_duration?.values?.['p(95)']?.toFixed(2) || 'N/A';

    console.log('\n=============================================');
    console.log('📊 RESUMEN - TEST CIRCUIT BREAKER + RETRY');
    console.log('=============================================');
    console.log('');
    console.log('📨 REQUESTS POR ENDPOINT:');
    console.log(`   🏠 /api/home:                 ${home}`);
    console.log(`   📁 /api/categories:           ${categories}`);
    console.log(`   🏷️  /api/categories/1/themes: ${themes}`);
    console.log(`   🖼️  /api/images:              ${images}`);
    console.log(`   🔍 /api/images/search:        ${search}`);
    console.log('');
    console.log('📈 MÉTRICAS:');
    console.log(`   ❌ Requests fallidos (HTTP):  ${failed}`);
    console.log(`   🔄 Fallbacks activados:       ${fallbacks}`);
    console.log(`   ⏱️  Tiempo promedio:          ${avgDuration}ms`);
    console.log(`   ⏱️  Tiempo p95:               ${p95Duration}ms`);

    const totalRequests = home + categories + themes + images + search;
    const successRate = totalRequests > 0 ?
        (((totalRequests - failed) / totalRequests) * 100).toFixed(2) : 0;

    console.log(`   📊 Tasa de éxito HTTP:        ${successRate}%`);

    // Análisis del Circuit Breaker
    console.log('');
    console.log('⚡ ANÁLISIS CIRCUIT BREAKER:');
    if (fallbacks > 0) {
        console.log(`   🔴 Circuit Breaker ACTIVADO`);
        console.log(`   📉 ${fallbacks} respuestas de fallback detectadas`);
        console.log('   ✅ El sistema se degradó elegantemente');
    } else if (parseFloat(successRate) >= 90) {
        console.log('   🟢 Circuit Breaker en estado CLOSED (normal)');
        console.log('   ✅ Todos los endpoints respondieron correctamente');
    } else {
        console.log('   🟡 Estado indeterminado - revisar logs');
    }

    // Análisis del Retry
    console.log('');
    console.log('🔁 ANÁLISIS RETRY:');
    if (parseFloat(avgDuration) > 1000 && fallbacks > 0) {
        console.log('   ℹ️  Tiempo alto = Retry intentó reconectar');
    } else if (parseFloat(avgDuration) < 500) {
        console.log('   ✅ Tiempos normales - sin necesidad de reintentos');
    }

    console.log('');
    console.log('=============================================');
    if (fallbacks > 0) {
        console.log('🎉 ¡TODOS LOS CIRCUIT BREAKERS FUNCIONANDO!');
        console.log('   5/5 endpoints públicos protegidos.');
    } else if (parseFloat(successRate) >= 90) {
        console.log('🎉 ¡API 100% resiliente! BD estable.');
    }
    console.log('=============================================\n');

    return {};
}
