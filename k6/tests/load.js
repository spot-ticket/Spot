// Load Test
// 일반적인 부하 상황에서의 성능 테스트
import { sleep, group } from 'k6';
import { env, k6Thresholds } from '../config/index.js';
import { login, loginAllUsers } from '../lib/auth.js';
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
  const tokens = {
    customer: login('customer'),
    owner: login('owner'),
  };

  return tokens;
}

// Customer Browse Scenario
export function customerBrowseScenario(data) {
  const token = data.customer?.accessToken;
  if (!token) return;

  group('Customer: Browse Stores', () => {
    // 1. Get categories
    const categories = testGetCategories(token);
    thinkTime(1);

    // 2. Browse stores
    const stores = testGetStores(token, { page: 0, size: 10 });
    thinkTime(2);

    // 3. View store detail
    testGetStore(token, env.testData.storeId);
    thinkTime(1);

    // 4. View menus
    const menus = testGetMenus(token, env.testData.storeId);
    thinkTime(1);

    // 5. View menu detail
    testGetMenu(token, env.testData.storeId, env.testData.menuId);
    thinkTime(1);

    // 6. Check reviews
    testGetStoreReviewStats(env.testData.storeId);
    testGetStoreReviews(env.testData.storeId, { page: 0, size: 5 });
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
