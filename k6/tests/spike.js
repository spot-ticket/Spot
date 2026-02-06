// Spike Test
// 급격한 부하 증가에 대한 시스템 반응 테스트
import http from 'k6/http';
import { sleep, group } from 'k6';
import { env, endpoints, buildUrl, k6Thresholds } from '../config/index.js';
import { login, getAuthHeaders } from '../lib/auth.js';

// Scenarios
import { testLogin } from '../scenarios/user/auth.js';
import { testGetStores, testGetStore } from '../scenarios/store/store.js';
import { testGetMenus, testGetMenu } from '../scenarios/store/menu.js';
import { testGetCategories } from '../scenarios/store/category.js';

// Helper: random item from array
function randomItem(arr) {
  return arr && arr.length > 0 ? arr[Math.floor(Math.random() * arr.length)] : null;
}

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

  const customerTokens = login('customer');

  if (!customerTokens.accessToken) {
    console.error('Login failed!');
    return { customer: null, stores: [], menus: {} };
  }

  // Fetch stores for spike testing
  const storesRes = http.get(buildUrl(endpoints.store.getStores) + '?page=0&size=30', {
    headers: getAuthHeaders(customerTokens.accessToken),
  });

  let stores = [];
  let menusCache = {};

  if (storesRes.status === 200) {
    try {
      const storesData = JSON.parse(storesRes.body);
      stores = storesData.content || storesData;
      console.log(`Fetched ${stores.length} stores for spike testing`);

      // Cache menus for first 3 stores (quick setup for spike test)
      for (let i = 0; i < Math.min(3, stores.length); i++) {
        const store = stores[i];
        const storeId = store.id || store.storeId;

        const menusRes = http.get(buildUrl(endpoints.store.getMenus(storeId)), {
          headers: getAuthHeaders(customerTokens.accessToken),
        });

        if (menusRes.status === 200) {
          try {
            const menusData = JSON.parse(menusRes.body);
            menusCache[storeId] = menusData.content || menusData;
          } catch (e) {}
        }
      }
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

export function spikeScenario(data) {
  const token = data.customer?.accessToken;
  if (!token) return;

  const stores = data.stores || [];
  const menusCache = data.menus || {};

  group('Spike Load Operations', () => {
    // Quick succession of requests
    testGetStores(token, { page: 0, size: 10 });
    sleep(0.1);

    // Select random store
    const selectedStore = randomItem(stores);
    if (!selectedStore) return;

    const storeId = selectedStore.id || selectedStore.storeId;

    testGetStore(token, storeId);
    sleep(0.1);

    const menus = testGetMenus(token, storeId);
    sleep(0.1);

    // Use fetched or cached menus
    const availableMenus = menus || menusCache[storeId];
    if (availableMenus && availableMenus.length > 0) {
      const selectedMenu = randomItem(availableMenus);
      const menuId = selectedMenu.id || selectedMenu.menuId;
      testGetMenu(token, storeId, menuId);
    }
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
