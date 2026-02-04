// Load Test
// 일반적인 부하 상황에서의 성능 테스트
import http from 'k6/http';
import { sleep, group } from 'k6';
import { env, endpoints, buildUrl, k6Thresholds } from '../config/index.js';
import { login, loginAllUsers, getAuthHeaders } from '../lib/auth.js';
import { randomItem, thinkTime } from '../lib/helpers.js';

// Scenarios
import { testLogin } from '../scenarios/user/auth.js';
import { testGetStores, testGetStore, testSearchStores } from '../scenarios/store/store.js';
import { testGetMenus, testGetMenu } from '../scenarios/store/menu.js';
import { testGetCategories, testGetStoresByCategory } from '../scenarios/store/category.js';
import { testGetStoreReviews, testGetStoreReviewStats } from '../scenarios/store/review.js';
import { testGetMyOrders, testGetMyActiveOrders } from '../scenarios/order/customer.js';
import { testGetMyStoreOrders, testGetMyStoreActiveOrders } from '../scenarios/order/owner.js';
import { testCheckBillingKeyExists } from '../scenarios/payment/payment.js';

export const options = {
  scenarios: {
    // Customer browsing scenario
    customer_browse: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 10 },   // ramp up
        { duration: '3m', target: 10 },   // steady state
        { duration: '1m', target: 0 },    // ramp down
      ],
      exec: 'customerBrowseScenario',
      tags: { scenario: 'customer_browse' },
    },

    // Owner management scenario
    owner_manage: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 5 },
        { duration: '3m', target: 5 },
        { duration: '1m', target: 0 },
      ],
      exec: 'ownerManageScenario',
      tags: { scenario: 'owner_manage' },
    },
  },
  thresholds: {
    ...k6Thresholds.global,
    ...k6Thresholds.store,
    ...k6Thresholds.order,
  },
};

export function setup() {
  console.log('='.repeat(50));
  console.log('  LOAD TEST - Normal Load Conditions');
  console.log('='.repeat(50));
  console.log(`Base URL: ${env.baseUrl}`);

  // Login multiple users
  const customerTokens = login('customer');
  const ownerTokens = login('owner');

  if (!customerTokens.accessToken) {
    console.error('Customer login failed!');
    return { customer: null, owner: ownerTokens, stores: [], menus: {} };
  }

  // Dynamically fetch stores
  const storesRes = http.get(buildUrl(endpoints.store.getStores) + '?page=0&size=30', {
    headers: getAuthHeaders(customerTokens.accessToken),
  });

  let stores = [];
  let menusCache = {};

  if (storesRes.status === 200) {
    try {
      const storesData = JSON.parse(storesRes.body);
      stores = storesData.content || storesData;

      console.log(`Fetched ${stores.length} stores for load testing`);

      // Cache menus for first 5 stores
      for (let i = 0; i < Math.min(5, stores.length); i++) {
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
    owner: ownerTokens,
    stores: stores,
    menus: menusCache,
  };
}

// Customer Browse Scenario
export function customerBrowseScenario(data) {
  const token = data.customer?.accessToken;
  if (!token) return;

  const stores = data.stores || [];
  const menusCache = data.menus || {};

  group('Customer: Browse Stores', () => {
    // 1. Get categories
    const categories = testGetCategories(token);
    thinkTime(1);

    // 2. Browse stores
    const storesList = testGetStores(token, { page: 0, size: 10 });
    thinkTime(2);

    // Select random store from available stores
    const selectedStore = randomItem(stores);
    if (!selectedStore) return;

    const storeId = selectedStore.id || selectedStore.storeId;

    // 3. View store detail
    testGetStore(token, storeId);
    thinkTime(1);

    // 4. View menus
    const menus = testGetMenus(token, storeId);
    thinkTime(1);

    // 5. View menu detail
    const availableMenus = menus || menusCache[storeId];
    if (availableMenus && availableMenus.length > 0) {
      const selectedMenu = randomItem(availableMenus);
      const menuId = selectedMenu.id || selectedMenu.menuId;
      testGetMenu(token, storeId, menuId);
    }
    thinkTime(1);

    // 6. Check reviews
    testGetStoreReviewStats(storeId);
    testGetStoreReviews(storeId, { page: 0, size: 5 });
    thinkTime(2);
  });

  group('Customer: Check Orders', () => {
    testGetMyOrders(token, { page: 0, size: 5 });
    thinkTime(1);

    testGetMyActiveOrders(token);
    thinkTime(1);
  });
}

// Owner Management Scenario
export function ownerManageScenario(data) {
  const token = data.owner?.accessToken;
  if (!token) return;

  group('Owner: Manage Orders', () => {
    // 1. Check active orders
    testGetMyStoreActiveOrders(token);
    thinkTime(2);

    // 2. View order history
    testGetMyStoreOrders(token, { page: 0, size: 10 });
    thinkTime(3);
  });
}

export function teardown(data) {
  console.log('='.repeat(50));
  console.log('  LOAD TEST COMPLETED');
  console.log('='.repeat(50));
}
