// Stress Test
// 시스템의 한계를 테스트하는 스트레스 테스트
import { sleep, group } from 'k6';
import { env, k6Thresholds } from '../config/index.js';
import { login } from '../lib/auth.js';
import { thinkTime } from '../lib/helpers.js';

// Scenarios
import { testLogin } from '../scenarios/user/auth.js';
import { testGetStores, testGetStore, testSearchStores } from '../scenarios/store/store.js';
import { testGetMenus, testGetMenu } from '../scenarios/store/menu.js';
import { testGetCategories } from '../scenarios/store/category.js';
import { testGetMyOrders, testGetMyActiveOrders } from '../scenarios/order/customer.js';

export const options = {
  scenarios: {
    // Stress test - gradually increase load
    stress_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 10 },    // warm up
        { duration: '2m', target: 20 },    // normal load
        { duration: '2m', target: 50 },    // high load
        { duration: '2m', target: 100 },   // stress load
        { duration: '3m', target: 100 },   // sustain stress
        { duration: '2m', target: 50 },    // recovery
        { duration: '1m', target: 0 },     // cool down
      ],
      exec: 'stressScenario',
      tags: { scenario: 'stress' },
    },
  },
  thresholds: {
    // More relaxed thresholds for stress test
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.1'],  // Allow up to 10% failure
  },
};

export function setup() {
  console.log('='.repeat(50));
  console.log('  STRESS TEST - Finding System Limits');
  console.log('='.repeat(50));
  console.log(`Base URL: ${env.baseUrl}`);

  // Login as customer
  const tokens = {
    customer: login('customer'),
  };

  return tokens;
}

export function stressScenario(data) {
  const token = data.customer?.accessToken;
  if (!token) return;

  group('High Load Operations', () => {
    // 1. Authentication check
    testLogin('customer');
    sleep(0.5);

    // 2. Store browsing
    testGetStores(token, { page: 0, size: 20 });
    sleep(0.3);

    // 3. Store detail
    testGetStore(token, env.testData.storeId);
    sleep(0.3);

    // 4. Menu browsing
    testGetMenus(token, env.testData.storeId);
    sleep(0.3);

    // 5. Menu detail
    testGetMenu(token, env.testData.storeId, env.testData.menuId);
    sleep(0.3);

    // 6. Categories
    testGetCategories(token);
    sleep(0.3);

    // 7. Search
    testSearchStores(token, 'test', { page: 0, size: 10 });
    sleep(0.3);

    // 8. Orders
    testGetMyOrders(token, { page: 0, size: 10 });
    sleep(0.3);

    // 9. Active orders
    testGetMyActiveOrders(token);
    sleep(0.5);
  });
}

export function teardown(data) {
  console.log('='.repeat(50));
  console.log('  STRESS TEST COMPLETED');
  console.log('='.repeat(50));
}
