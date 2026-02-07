import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

/**
 * ============================================
 * TEST DE RATE LIMITER - Endpoint de Login
 * ============================================
 *
 * Verifica que el Rate Limiter (authRL) funciona.
 * Configuración: 10 requests/segundo máximo.
 *
 * Ejecutar: k6 run test-rate-limiter.js
 */

const rateLimitedRequests = new Counter('rate_limited_requests');
const successfulRequests = new Counter('successful_requests');

export const options = {
    scenarios: {
        rate_limiter_test: {
            executor: 'constant-arrival-rate',
            rate: 25,              // 25 req/s (más del doble del límite de 10)
            timeUnit: '1s',
            duration: '15s',
            preAllocatedVUs: 15,
            maxVUs: 50,
        },
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const loginPayload = JSON.stringify({
        username: 'test_user',
        password: 'test_password'
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const response = http.post(`${BASE_URL}/api/auth/login`, loginPayload, params);

    if (response.status === 429) {
        rateLimitedRequests.add(1);
    } else {
        successfulRequests.add(1);
    }

    check(response, {
        'response received': (r) => r.status !== 0,
        'valid response (429, 200, 401, 403)': (r) =>
            [429, 200, 401, 403].includes(r.status),
    });
}

export function handleSummary(data) {
    const rateLimited = data.metrics.rate_limited_requests?.values?.count || 0;
    const successful = data.metrics.successful_requests?.values?.count || 0;

    console.log('\n========================================');
    console.log('📊 RESUMEN - TEST RATE LIMITER');
    console.log('========================================');
    console.log(`✅ Peticiones pasaron: ${successful}`);
    console.log(`⛔ Peticiones bloqueadas (429): ${rateLimited}`);
    console.log(`📈 Total: ${successful + rateLimited}`);

    if (rateLimited > 0) {
        console.log('\n🎉 ¡Rate Limiter FUNCIONA correctamente!');
    } else {
        console.log('\n⚠️ Ninguna petición bloqueada - verifica configuración');
    }
    console.log('========================================\n');

    return {};
}
