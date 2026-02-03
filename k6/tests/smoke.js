// Smoke Test
// 빠른 검증 테스트 - 시스템이 정상 동작하는지 확인
// TEST_MODE: browse (조회만), integrity (정합성 검증), all (전체)
import http from 'k6/http';
import { sleep } from 'k6';
import { env, endpoints, buildUrl, k6Thresholds } from '../config/index.js';
import { login, getAuthHeaders } from '../lib/auth.js';

// Scenarios
import { testLogin } from '../scenarios/user/auth.js';
import { testGetStores, testGetStore } from '../scenarios/store/store.js';
import { testGetMenus, testGetMenu } from '../scenarios/store/menu.js';
import { testGetCategories } from '../scenarios/store/category.js';
import { testGetMyOrders, testCreateOrderDynamic } from '../scenarios/order/customer.js';

// Test Mode from environment
const TEST_MODE = __ENV.TEST_MODE || 'all';       // browse, integrity, all
const BROWSE_MODE = __ENV.BROWSE_MODE || 'random'; // random, fixed

// Helper: random item from array
function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

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
  console.log(`  Test Mode: ${TEST_MODE}`);
  console.log(`  Browse Mode: ${BROWSE_MODE}`);
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

  // ==========================================
  // Browse Mode: 조회 테스트만 실행
  // ==========================================
  if (TEST_MODE === 'browse' || TEST_MODE === 'all') {
    // 1. Test Authentication
    testLogin('customer');
    sleep(1);

    // 2. Test Store List
    const stores = testGetStores(customerToken, { page: 0, size: 50 });
    sleep(1);

    let storeId, menuId;

    if (BROWSE_MODE === 'random' && stores?.content?.length > 0) {
      // Random Mode: 매번 랜덤하게 store/menu 선택
      const selectedStore = randomItem(stores.content);
      storeId = selectedStore.id || selectedStore.storeId;
    } else {
      // Fixed Mode: setup에서 가져온 고정 store/menu 사용
      storeId = testData?.storeId || (stores?.content?.[0]?.id);
    }

    // 3. Test Store Detail
    if (storeId) {
      testGetStore(customerToken, storeId);
      sleep(1);

      // 4. Test Menu List
      const menus = testGetMenus(customerToken, storeId);
      sleep(1);

      // 5. Test Menu Detail
      if (BROWSE_MODE === 'random' && menus?.length > 0) {
        // Random Mode: 랜덤 메뉴 선택
        const selectedMenu = randomItem(menus);
        menuId = selectedMenu.id || selectedMenu.menuId;
      } else {
        // Fixed Mode: 첫 번째 메뉴 사용
        menuId = testData?.menuId || (menus?.[0]?.id);
      }

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

  // ==========================================
  // Integrity Mode: 데이터 정합성 검증
  // ==========================================
  if (TEST_MODE === 'integrity' || TEST_MODE === 'all') {
    console.log('Running data integrity tests...');

    // 동적 주문 생성 + 정합성 검증
    // - OPEN 상태 store 필터링
    // - 가격 정합성 검증
    // - 주문 내역 반영 확인
    const orderResult = testCreateOrderDynamic(customerToken, { validateIntegrity: true });

    if (orderResult) {
      console.log(`Integrity test order created: ${orderResult.id || orderResult.orderId}`);
    } else {
      console.log('Integrity test: No order created (may be expected if no OPEN stores)');
    }
    sleep(1);
  }
}

export function teardown(data) {
  console.log('='.repeat(50));
  console.log(`  SMOKE TEST COMPLETED (Mode: ${TEST_MODE})`);
  console.log('='.repeat(50));
  if (TEST_MODE === 'integrity' || TEST_MODE === 'all') {
    console.log('Check metrics for data integrity results:');
    console.log('  - price_integrity_errors');
    console.log('  - order_reflection_errors');
    console.log('  - store_status_errors');
  }
}
