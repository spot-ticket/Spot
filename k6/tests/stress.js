// Stress Test
// 시스템의 한계를 테스트하는 스트레스 테스트
import http from 'k6/http';
import { sleep, group } from 'k6';
import { env, endpoints, buildUrl, k6Thresholds } from '../config/index.js';
import { login, getAuthHeaders } from '../lib/auth.js';
import { thinkTime } from '../lib/helpers.js';

// Scenarios
import { testLogin } from '../scenarios/user/auth.js';
import { testGetStores, testGetStore, testSearchStores } from '../scenarios/store/store.js';
import { testGetMenus, testGetMenu } from '../scenarios/store/menu.js';
import { testGetCategories } from '../scenarios/store/category.js';
import { testGetMyOrders, testGetMyActiveOrders } from '../scenarios/order/customer.js';

// Helper: random item from array
function randomItem(arr) {
  return arr && arr.length > 0 ? arr[Math.floor(Math.random() * arr.length)] : null;
}

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
  const customerTokens = login('customer');

  if (!customerTokens.accessToken) {
    console.error('Login failed!');
    return { customer: null, stores: [], menus: {} };
  }

  // Dynamically fetch stores
  const storesRes = http.get(buildUrl(endpoints.store.getStores) + '?page=0&size=50', {
    headers: getAuthHeaders(customerTokens.accessToken),
  });

  let stores = [];
  let menusCache = {};

  if (storesRes.status === 200) {
    try {
      const storesData = JSON.parse(storesRes.body);
      stores = storesData.content || storesData;

      console.log(`Fetched ${stores.length} stores for stress testing`);

      // Fetch menus for first 10 stores to cache
      for (let i = 0; i < Math.min(10, stores.length); i++) {
        const store = stores[i];
        const storeId = store.id || store.storeId;

        const menusRes = http.get(buildUrl(endpoints.store.getMenus(storeId)), {
          headers: getAuthHeaders(customerTokens.accessToken),
        });

        if (menusRes.status === 200) {
          try {
            const menusData = JSON.parse(menusRes.body);
            menusCache[storeId] = menusData.content || menusData;
          } catch (e) {
            console.error(`Failed to parse menus for store ${storeId}`);
          }
        }
      }

      console.log(`Cached menus for ${Object.keys(menusCache).length} stores`);
    } catch (e) {
      console.error('Failed to parse stores response');
    }
  }

  return {
    customer: customerTokens,
    stores: stores,
    menus: menusCache,
  };
}

export function stressScenario(data) {
  const token = data.customer?.accessToken;
  if (!token) return;

  const stores = data.stores || [];
  const menusCache = data.menus || {};

  group('High Load Operations', () => {
    // 1. Authentication check
    testLogin('customer');
    sleep(0.5);

    // 2. Store browsing
    testGetStores(token, { page: 0, size: 20 });
    sleep(0.3);

    // Select a random store from available stores
    const selectedStore = randomItem(stores);
    if (!selectedStore) {
      console.warn('No stores available for testing');
      return;
    }

    const storeId = selectedStore.id || selectedStore.storeId;

    // 3. Store detail
    testGetStore(token, storeId);
    sleep(0.3);

    // 4. Menu browsing
    const menus = testGetMenus(token, storeId);
    sleep(0.3);

    // 5. Menu detail - use fetched menus or cached menus
    const availableMenus = menus || menusCache[storeId];
    if (availableMenus && availableMenus.length > 0) {
      const selectedMenu = randomItem(availableMenus);
      const menuId = selectedMenu.id || selectedMenu.menuId;
      testGetMenu(token, storeId, menuId);
    }
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
