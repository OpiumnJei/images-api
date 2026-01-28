import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Counter, Trend } from 'k6/metrics';

/**
 * ============================================
 * TEST DE CARGA COMBINADO - TODOS LOS PATRONES
 * ============================================
 *
 * Este test prueba todos los patrones de resiliencia:
 * - Rate Limiter (endpoint de login)
 * - Circuit Breaker + Retry (todos los endpoints públicos)
 *
 * Ejecutar: k6 run test-load-combined.js
 */

// Métricas personalizadas
const rateLimited = new Counter('rate_limited');
const fallbackActivated = new Counter('fallback_activated');
const totalErrors = new Counter('total_errors');

export const options = {
    stages: [
        { duration: '15s', target: 20 },   // Calentamiento
        { duration: '30s', target: 50 },   // Carga media
        { duration: '30s', target: 100 },  // Carga alta
        { duration: '15s', target: 0 },    // Enfriamiento
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.3'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    // ==========================================
    // GRUPO 1: Test de Rate Limiter (Login)
    // ==========================================
    group('Rate Limiter - Auth', function () {
        const loginPayload = JSON.stringify({
            username: 'test_user',
            password: 'test_password'
        });

        const response = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
            headers: { 'Content-Type': 'application/json' },
        });

        if (response.status === 429) {
            rateLimited.add(1);
        } else if (response.status >= 500) {
            totalErrors.add(1);
        }

        check(response, {
            'auth: response received': (r) => r.status !== 0,
        });
    });

    sleep(0.2);

    // ==========================================
    // GRUPO 2: Circuit Breaker - Home
    // ==========================================
    group('Circuit Breaker - Home', function () {
        const response = http.get(`${BASE_URL}/api/home`);

        if (response.status === 200) {
            try {
                const body = JSON.parse(response.body);
                if (body.type === 'FALLBACK') {
                    fallbackActivated.add(1);
                }
            } catch (e) {}
        } else if (response.status >= 500) {
            totalErrors.add(1);
        }

        check(response, {
            'home: status 200': (r) => r.status === 200,
        });
    });

    sleep(0.2);

    // ==========================================
    // GRUPO 3: Circuit Breaker - Categories (NUEVO)
    // ==========================================
    group('Circuit Breaker - Categories', function () {
        const response = http.get(`${BASE_URL}/api/categories`);

        if (response.status === 200) {
            try {
                const body = JSON.parse(response.body);
                if (Array.isArray(body) && body.length === 0) {
                    fallbackActivated.add(1);
                }
            } catch (e) {}
        } else if (response.status >= 500) {
            totalErrors.add(1);
        }

        check(response, {
            'categories: status 200': (r) => r.status === 200,
        });
    });

    sleep(0.2);

    // ==========================================
    // GRUPO 4: Circuit Breaker - Themes (NUEVO)
    // ==========================================
    group('Circuit Breaker - Themes', function () {
        const response = http.get(`${BASE_URL}/api/categories/1/themes`);

        if (response.status === 200) {
            try {
                const body = JSON.parse(response.body);
                if (Array.isArray(body) && body.length === 0) {
                    fallbackActivated.add(1);
                }
            } catch (e) {}
        } else if (response.status >= 500) {
            totalErrors.add(1);
        }

        check(response, {
            'themes: status 200 o 404': (r) => r.status === 200 || r.status === 404,
        });
    });

    sleep(0.2);

    // ==========================================
    // GRUPO 5: Circuit Breaker - Images
    // ==========================================
    group('Circuit Breaker - Images', function () {
        const response = http.get(`${BASE_URL}/api/images`);

        if (response.status === 200) {
            try {
                const body = JSON.parse(response.body);
                if (body.content?.length === 0 && body.totalElements === 0) {
                    fallbackActivated.add(1);
                }
            } catch (e) {}
        } else if (response.status >= 500) {
            totalErrors.add(1);
        }

        check(response, {
            'images: status 200': (r) => r.status === 200,
        });
    });

    sleep(0.2);

    // ==========================================
    // GRUPO 6: Circuit Breaker - Search
    // ==========================================
    group('Circuit Breaker - Search', function () {
        const searchTerms = ['amor', 'feliz', 'navidad', 'cumple'];
        const term = searchTerms[Math.floor(Math.random() * searchTerms.length)];

        const response = http.get(`${BASE_URL}/api/images/search?q=${term}`);

        if (response.status === 200) {
            try {
                const body = JSON.parse(response.body);
                if (body.content?.length === 0 && body.totalElements === 0) {
                    fallbackActivated.add(1);
                }
            } catch (e) {}
        } else if (response.status >= 500) {
            totalErrors.add(1);
        }

        check(response, {
            'search: status 200': (r) => r.status === 200,
        });
    });

    sleep(0.2);
}

export function handleSummary(data) {
    const rl = data.metrics.rate_limited?.values?.count || 0;
    const fb = data.metrics.fallback_activated?.values?.count || 0;
    const errors = data.metrics.total_errors?.values?.count || 0;
    const totalReqs = data.metrics.http_reqs?.values?.count || 0;
    const avgDuration = data.metrics.http_req_duration?.values?.avg?.toFixed(2) || 'N/A';

    console.log('\n================================================');
    console.log('📊 RESUMEN - TEST DE CARGA COMBINADO');
    console.log('================================================');
    console.log(`📨 Total de requests: ${totalReqs}`);
    console.log(`⏱️  Tiempo promedio: ${avgDuration}ms`);
    console.log('');
    console.log('🔒 RATE LIMITER:');
    console.log(`   ⛔ Peticiones bloqueadas (429): ${rl}`);
    console.log('');
    console.log('⚡ CIRCUIT BREAKER:');
    console.log(`   🔄 Fallbacks activados: ${fb}`);
    console.log('');
    console.log('❌ ERRORES:');
    console.log(`   💥 Errores de servidor (5xx): ${errors}`);
    console.log('');

    // Análisis
    console.log('📋 ANÁLISIS:');
    if (rl > 0) {
        console.log('   ✅ Rate Limiter: FUNCIONANDO');
    } else {
        console.log('   ⚠️  Rate Limiter: Sin actividad');
    }

    if (fb > 0) {
        console.log('   ✅ Circuit Breaker: FUNCIONANDO (fallbacks activos)');
    } else {
        console.log('   ℹ️  Circuit Breaker: Sin activación (BD estable)');
    }

    if (errors === 0) {
        console.log('   ✅ Sin errores 500');
    } else {
        console.log(`   ⚠️  ${errors} errores - revisar logs`);
    }

    console.log('');
    console.log('================================================');
    console.log('🔗 ENDPOINTS PROTEGIDOS (6/6):');
    console.log('   ✅ POST /api/auth/login     (Rate Limiter)');
    console.log('   ✅ GET  /api/home           (CB + Retry)');
    console.log('   ✅ GET  /api/categories     (CB + Retry)');
    console.log('   ✅ GET  /api/categories/*/themes (CB + Retry)');
    console.log('   ✅ GET  /api/images         (CB + Retry)');
    console.log('   ✅ GET  /api/images/search  (CB + Retry)');
    console.log('================================================\n');

    return {};
}
