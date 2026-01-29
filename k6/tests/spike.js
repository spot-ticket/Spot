// Spike Test
// 급격한 부하 증가에 대한 시스템 반응 테스트
import { sleep, group } from 'k6';
import { env, k6Thresholds } from '../config/index.js';
import { login } from '../lib/auth.js';

// Scenarios
import { testLogin } from '../scenarios/user/auth.js';
import { testGetStores, testGetStore } from '../scenarios/store/store.js';
import { testGetMenus, testGetMenu } from '../scenarios/store/menu.js';
import { testGetCategories } from '../scenarios/store/category.js';

export const options = {
  scenarios: {
    // Spike test - sudden traffic spike
    spike_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 5 },     // warm up
        { duration: '10s', target: 100 },   // SPIKE!
        { duration: '1m', target: 100 },    // sustain spike
        { duration: '10s', target: 5 },     // recover
        { duration: '30s', target: 0 },     // cool down
      ],
      exec: 'spikeScenario',
      tags: { scenario: 'spike' },
    },
  },
  thresholds: {
    // Relaxed thresholds during spike
    http_req_duration: ['p(95)<3000', 'p(99)<10000'],
    http_req_failed: ['rate<0.2'],  // Allow up to 20% failure during spike
  },
};

export function setup() {
  console.log('='.repeat(50));
  console.log('  SPIKE TEST - Sudden Traffic Surge');
  console.log('='.repeat(50));
  console.log(`Base URL: ${env.baseUrl}`);

  const tokens = {
    customer: login('customer'),
  };

  return tokens;
}

export function spikeScenario(data) {
  const token = data.customer?.accessToken;
  if (!token) return;

  group('Spike Load Operations', () => {
    // Quick succession of requests
    testGetStores(token, { page: 0, size: 10 });
    sleep(0.1);

    testGetStore(token, env.testData.storeId);
    sleep(0.1);

    testGetMenus(token, env.testData.storeId);
    sleep(0.1);

    testGetMenu(token, env.testData.storeId, env.testData.menuId);
    sleep(0.1);

    testGetCategories(token);
    sleep(0.2);
  });
}

export function teardown(data) {
  console.log('='.repeat(50));
  console.log('  SPIKE TEST COMPLETED');
  console.log('='.repeat(50));
}
