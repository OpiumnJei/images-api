import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * ============================================
 * TEST SIMPLE - VERIFICAR ENDPOINTS (SMOKE TEST)
 * ============================================
 *
 * 📚 ¿QUÉ ES K6?
 * k6 es una herramienta de testing de carga/rendimiento.
 * Permite simular múltiples usuarios haciendo peticiones a tu API.
 *
 * 📚 ¿QUÉ ES UN SMOKE TEST?
 * Es el test más básico: verifica que los endpoints responden.
 * Se ejecuta con pocos usuarios (1-5) para detectar errores obvios.
 * Si el smoke test falla, no tiene sentido hacer tests de carga.
 *
 * 📚 CONCEPTOS CLAVE:
 * - VU (Virtual User): Usuario virtual simulado
 * - Duration: Tiempo que dura el test
 * - Check: Verificación/aserción (como un assert)
 * - Sleep: Pausa entre peticiones (simula comportamiento real)
 *
 * Ejecutar: k6 run test-smoke.js
 */

/**
 * 📚 OPTIONS - Configuración del test
 *
 * vus: Número de "Virtual Users" (usuarios simulados)
 *      Con vus=1, simulas UN solo usuario haciendo peticiones.
 *
 * duration: Cuánto tiempo corre el test.
 *           Durante 15s, el usuario virtual repite el ciclo de pruebas.
 */
export const options = {
    vus: 1,           // 1 usuario virtual (test básico)
    duration: '15s',  // Durante 15 segundos
};

// URL base de tu API (cámbiala si usas otro puerto)
const BASE_URL = 'http://localhost:8080';

/**
 * 📚 FUNCIÓN DEFAULT - El "cuerpo" del test
 *
 * Esta función se ejecuta UNA VEZ por cada "iteración" del usuario virtual.
 * Con duration='15s', se repetirá varias veces durante esos 15 segundos.
 *
 * Cada iteración prueba todos los endpoints secuencialmente.
 */
export default function () {
    console.log('🔍 Probando endpoints...');

    // ==========================================
    // Test 1: GET /api/home
    // ==========================================
    // http.get() hace una petición GET y devuelve la respuesta
    const homeResp = http.get(`${BASE_URL}/api/home`);

    // check() verifica condiciones (como un assert)
    // Si falla, k6 lo registra pero NO detiene el test
    check(homeResp, {
        '✅ /api/home responde 200': (r) => r.status === 200,
    });
    console.log(`   /api/home: ${homeResp.status}`);

    // sleep() pausa la ejecución (en segundos)
    // Simula el tiempo que un usuario real tarda entre acciones
    sleep(0.5);

    // ==========================================
    // Test 2: GET /api/categories
    // ==========================================
    const categoriesResp = http.get(`${BASE_URL}/api/categories`);
    check(categoriesResp, {
        '✅ /api/categories responde 200': (r) => r.status === 200,
    });
    console.log(`   /api/categories: ${categoriesResp.status}`);
    sleep(0.5);

    // ==========================================
    // Test 3: GET /api/categories/1/themes
    // ==========================================
    // Nota: Aceptamos 404 porque la categoría 1 podría no existir
    const themesResp = http.get(`${BASE_URL}/api/categories/1/themes`);
    check(themesResp, {
        '✅ /api/categories/1/themes responde 200 o 404': (r) =>
            r.status === 200 || r.status === 404,
    });
    console.log(`   /api/categories/1/themes: ${themesResp.status}`);
    sleep(0.5);

    // ==========================================
    // Test 4: GET /api/images
    // ==========================================
    const imagesResp = http.get(`${BASE_URL}/api/images`);
    check(imagesResp, {
        '✅ /api/images responde 200': (r) => r.status === 200,
    });
    console.log(`   /api/images: ${imagesResp.status}`);
    sleep(0.5);

    // ==========================================
    // Test 5: GET /api/images/search
    // ==========================================
    const searchResp = http.get(`${BASE_URL}/api/images/search?q=test`);
    check(searchResp, {
        '✅ /api/images/search responde 200': (r) => r.status === 200,
    });
    console.log(`   /api/images/search: ${searchResp.status}`);
    sleep(0.5);

    // ==========================================
    // Test 6: GET /actuator/health
    // ==========================================
    // Este endpoint de Spring Boot Actuator muestra el estado de salud
    const healthResp = http.get(`${BASE_URL}/actuator/health`);
    check(healthResp, {
        '✅ /actuator/health responde 200': (r) => r.status === 200,
    });
    console.log(`   /actuator/health: ${healthResp.status}`);
    sleep(0.5);
}

/**
 * 📚 HANDLE SUMMARY - Resumen al finalizar
 *
 * Esta función se ejecuta UNA SOLA VEZ al terminar el test.
 * Recibe 'data' con todas las métricas recopiladas.
 *
 * Métricas importantes:
 * - checks.passes: Cuántos checks pasaron
 * - checks.fails: Cuántos checks fallaron
 * - http_req_duration: Tiempos de respuesta
 * - http_reqs: Total de peticiones HTTP
 */
export function handleSummary(data) {
    const checks = data.metrics.checks;
    const passed = checks?.values?.passes || 0;
    const failed = checks?.values?.fails || 0;

    console.log('\n========================================');
    console.log('📊 RESUMEN - SMOKE TEST');
    console.log('========================================');
    console.log(`✅ Checks pasados: ${passed}`);
    console.log(`❌ Checks fallidos: ${failed}`);
    console.log('');
    console.log('📋 ENDPOINTS VERIFICADOS:');
    console.log('   • GET /api/home');
    console.log('   • GET /api/categories');
    console.log('   • GET /api/categories/{id}/themes');
    console.log('   • GET /api/images');
    console.log('   • GET /api/images/search');
    console.log('   • GET /actuator/health');

    if (failed === 0) {
        console.log('\n🎉 ¡Todos los endpoints funcionan!');
    } else {
        console.log('\n⚠️ Algunos endpoints fallaron.');
    }
    console.log('========================================\n');

    // Retornar {} significa que no generamos archivos de reporte adicionales
    return {};
}
