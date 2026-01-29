// Smoke Test
// 빠른 검증 테스트 - 시스템이 정상 동작하는지 확인
import http from 'k6/http';
import { sleep } from 'k6';
import { env, endpoints, buildUrl, k6Thresholds } from '../config/index.js';
import { login, getAuthHeaders } from '../lib/auth.js';

// Scenarios
import { testLogin } from '../scenarios/user/auth.js';
import { testGetStores, testGetStore } from '../scenarios/store/store.js';
import { testGetMenus, testGetMenu } from '../scenarios/store/menu.js';
import { testGetCategories } from '../scenarios/store/category.js';
import { testGetMyOrders } from '../scenarios/order/customer.js';

export const options = {
  vus: 1,
  duration: '30s',
  thresholds: {
    ...k6Thresholds.global,
    http_req_duration: ['p(95)<1000'],
  },
};

export function setup() {
  console.log('='.repeat(50));
  console.log('  SMOKE TEST - Quick Verification');
  console.log('='.repeat(50));
  console.log(`Base URL: ${env.baseUrl}`);

  // 1. Login as customer
  const customerTokens = login('customer');

  if (!customerTokens.accessToken) {
    console.error('Login failed!');
    return { customer: null, testData: null };
  }

  // 2. Dynamically fetch store data
  let storeId = null;
  let menuId = null;

  const storesRes = http.get(buildUrl(endpoints.store.getStores) + '?page=0&size=10', {
    headers: getAuthHeaders(customerTokens.accessToken),
  });

  if (storesRes.status === 200) {
    try {
      const storesData = JSON.parse(storesRes.body);
      const stores = storesData.content || storesData;

      if (stores && stores.length > 0) {
        storeId = stores[0].id || stores[0].storeId;
        console.log(`Found store: ${storeId}`);

        // 3. Fetch menus for this store
        const menusRes = http.get(buildUrl(endpoints.store.getMenus(storeId)), {
          headers: getAuthHeaders(customerTokens.accessToken),
        });

        if (menusRes.status === 200) {
          try {
            const menusData = JSON.parse(menusRes.body);
            const menus = menusData.content || menusData;

            if (menus && menus.length > 0) {
              menuId = menus[0].id || menus[0].menuId;
              console.log(`Found menu: ${menuId}`);
            }
          } catch (e) {
            console.error('Failed to parse menus response');
          }
        }
      }
    } catch (e) {
      console.error('Failed to parse stores response');
    }
  }

  console.log('='.repeat(50));
  console.log(`Store ID: ${storeId || 'not found'}`);
  console.log(`Menu ID: ${menuId || 'not found'}`);
  console.log('='.repeat(50));

  return {
    customer: customerTokens,
    testData: {
      storeId,
      menuId,
    },
  };
}

export default function (data) {
  const customerToken = data.customer?.accessToken;
  const testData = data.testData;

  if (!customerToken) {
    console.error('No customer token available');
    return;
  }

  // 1. Test Authentication
  testLogin('customer');
  sleep(1);

  // 2. Test Store List
  const stores = testGetStores(customerToken, { page: 0, size: 5 });
  sleep(1);

  // 3. Test Store Detail (dynamic)
  const storeId = testData?.storeId || (stores?.content?.[0]?.id);
  if (storeId) {
    testGetStore(customerToken, storeId);
    sleep(1);

    // 4. Test Menu List (dynamic)
    const menus = testGetMenus(customerToken, storeId);
    sleep(1);

    // 5. Test Menu Detail (dynamic)
    const menuId = testData?.menuId || (menus?.[0]?.id);
    if (menuId) {
      testGetMenu(customerToken, storeId, menuId);
      sleep(1);
    }
  }

  // 6. Test Categories
  testGetCategories(customerToken);
  sleep(1);

  // 7. Test Order List
  testGetMyOrders(customerToken, { page: 0, size: 5 });
  sleep(1);
}

export function teardown(data) {
  console.log('='.repeat(50));
  console.log('  SMOKE TEST COMPLETED');
  console.log('='.repeat(50));
}
