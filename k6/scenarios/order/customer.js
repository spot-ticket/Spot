// SPOT-ORDER: Customer Order Scenarios
import http from 'k6/http';
import { check, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { env, endpoints, buildUrl } from '../../config/index.js';
import { getAuthHeaders } from '../../lib/auth.js';
import { thinkTime, buildQueryString, randomItem } from '../../lib/helpers.js';
import { testGetStores } from '../store/store.js';
import { testGetMenus } from '../store/menu.js';

// Custom Metrics
const orderCreateDuration = new Trend('order_create_duration');
const orderCreateErrors = new Rate('order_create_errors');
const orderListDuration = new Trend('order_list_duration');
const orderListErrors = new Rate('order_list_errors');
const orderActiveDuration = new Trend('order_active_duration');
const orderCancelDuration = new Trend('order_cancel_duration');

// Data Integrity Metrics
const priceIntegrityErrors = new Rate('price_integrity_errors');
const orderReflectionErrors = new Rate('order_reflection_errors');
const storeStatusErrors = new Rate('store_status_errors');

/**
 * Create Order (Customer)
 */
export function testCreateOrder(accessToken, orderData) {
  const payload = JSON.stringify(orderData || {
    storeId: env.testData.storeId,
    items: [
      {
        menuId: env.testData.menuId,
        quantity: 1,
        options: [],
      },
    ],
    memo: 'Test order from k6',
  });

  const res = http.post(buildUrl(endpoints.order.createOrder), payload, {
    headers: getAuthHeaders(accessToken),
    tags: { name: 'POST /api/orders' },
  });

  const success = check(res, {
    'createOrder: status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'createOrder: response time < 1000ms': (r) => r.timings.duration < 1000,
  });

  orderCreateDuration.add(res.timings.duration);
  orderCreateErrors.add(!success);

  return success ? JSON.parse(res.body) : null;
}

/**
 * Get My Orders (Customer)
 */
export function testGetMyOrders(accessToken, params = {}) {
  const queryString = buildQueryString({
    storeId: params.storeId,
    date: params.date,
    status: params.status,
    page: params.page || 0,
    size: params.size || 10,
    sortBy: params.sortBy || 'createdAt',
    direction: params.direction || 'DESC',
  });

  const res = http.get(buildUrl(endpoints.order.getMyOrders) + queryString, {
    headers: getAuthHeaders(accessToken),
    tags: { name: 'GET /api/orders/my' },
  });

  const success = check(res, {
    'getMyOrders: status is 200': (r) => r.status === 200,
    'getMyOrders: response time < 500ms': (r) => r.timings.duration < 500,
  });

  orderListDuration.add(res.timings.duration);
  orderListErrors.add(!success);

  return success ? JSON.parse(res.body) : null;
}

/**
 * Get My Active Orders (Customer)
 */
export function testGetMyActiveOrders(accessToken) {
  const res = http.get(buildUrl(endpoints.order.getMyActiveOrders), {
    headers: getAuthHeaders(accessToken),
    tags: { name: 'GET /api/orders/my/active' },
  });

  const success = check(res, {
    'getMyActiveOrders: status is 200': (r) => r.status === 200,
    'getMyActiveOrders: response time < 500ms': (r) => r.timings.duration < 500,
  });

  orderActiveDuration.add(res.timings.duration);
  return success ? JSON.parse(res.body) : null;
}

/**
 * Cancel Order (Customer)
 */
export function testCustomerCancelOrder(accessToken, orderId, cancelData) {
  const payload = JSON.stringify(cancelData || {
    reason: 'Customer requested cancellation',
  });

  const res = http.patch(buildUrl(endpoints.order.customerCancelOrder(orderId)), payload, {
    headers: getAuthHeaders(accessToken),
    tags: { name: 'PATCH /api/orders/{orderId}/customer-cancel' },
  });

  const success = check(res, {
    'customerCancelOrder: status is 200': (r) => r.status === 200,
    'customerCancelOrder: response time < 500ms': (r) => r.timings.duration < 500,
  });

  orderCancelDuration.add(res.timings.duration);
  return success;
}

/**
 * Create Order with Dynamic Store/Menu Selection + Data Integrity Validation
 * 실시간으로 store 목록을 조회하고 랜덤 선택 후, 해당 store의 menu를 조회하여 주문 생성
 * 데이터 정합성 검증 포함
 */
export function testCreateOrderDynamic(accessToken, options = {}) {
  const { validateIntegrity = true } = options;

  // 1. Store 목록 조회
  const storesResult = testGetStores(accessToken, { page: 0, size: 50 });
  if (!storesResult || !storesResult.content || storesResult.content.length === 0) {
    console.warn('No stores available, falling back to fixed storeId');
    return testCreateOrder(accessToken);
  }

  // 2. OPEN 상태인 store만 필터링
  const openStores = storesResult.content.filter((store) => {
    const status = store.status || store.storeStatus;
    return status === 'OPEN' || status === 'open';
  });

  if (openStores.length === 0) {
    console.warn('No OPEN stores available');
    storeStatusErrors.add(1);
    // OPEN이 아닌 store에 주문 시도하여 실패 확인
    if (validateIntegrity && storesResult.content.length > 0) {
      const closedStore = randomItem(storesResult.content);
      const closedStoreId = closedStore.id || closedStore.storeId;
      const menus = testGetMenus(accessToken, closedStoreId);
      if (menus && menus.length > 0) {
        const testOrder = testCreateOrder(accessToken, {
          storeId: closedStoreId,
          items: [{ menuId: menus[0].id || menus[0].menuId, quantity: 1, options: [] }],
          memo: 'Testing closed store order rejection',
        });
        // 주문이 성공하면 정합성 오류
        if (testOrder) {
          console.error(`[INTEGRITY ERROR] Order created for non-OPEN store: ${closedStoreId}`);
          storeStatusErrors.add(1);
        }
      }
    }
    return null;
  }
  storeStatusErrors.add(0);

  // 3. 랜덤 OPEN store 선택
  const selectedStore = randomItem(openStores);
  const storeId = selectedStore.id || selectedStore.storeId;

  // 4. 선택된 store의 메뉴 목록 조회
  const menus = testGetMenus(accessToken, storeId);
  if (!menus || menus.length === 0) {
    console.warn(`No menus available for store ${storeId}`);
    return null;
  }

  // 5. 랜덤 메뉴 선택
  const selectedMenu = randomItem(menus);
  const menuId = selectedMenu.id || selectedMenu.menuId;
  const menuPrice = selectedMenu.price || 0;
  const quantity = Math.floor(Math.random() * 3) + 1;
  const expectedTotal = menuPrice * quantity;

  // 6. 동적으로 선택된 store/menu로 주문 생성
  const orderData = {
    storeId: storeId,
    items: [
      {
        menuId: menuId,
        quantity: quantity,
        options: [],
      },
    ],
    memo: `Dynamic order from k6 - Store: ${selectedStore.name || storeId}`,
  };

  const orderResult = testCreateOrder(accessToken, orderData);

  // 7. 데이터 정합성 검증
  if (validateIntegrity && orderResult) {
    // 가격 정합성 검증
    const orderTotal = orderResult.totalPrice || orderResult.totalAmount || 0;
    if (orderTotal > 0 && expectedTotal > 0 && orderTotal !== expectedTotal) {
      console.error(`[PRICE INTEGRITY ERROR] Expected: ${expectedTotal}, Got: ${orderTotal}`);
      priceIntegrityErrors.add(1);
    } else {
      priceIntegrityErrors.add(0);
    }

    // 주문 내역 반영 검증
    const orderId = orderResult.id || orderResult.orderId;
    if (orderId) {
      thinkTime(0.3); // 잠시 대기 후 조회
      const myOrders = testGetMyOrders(accessToken, { page: 0, size: 10 });
      if (myOrders && myOrders.content) {
        const foundOrder = myOrders.content.find(
          (o) => (o.id || o.orderId) === orderId
        );
        if (!foundOrder) {
          console.error(`[REFLECTION ERROR] Order ${orderId} not found in my orders`);
          orderReflectionErrors.add(1);
        } else {
          orderReflectionErrors.add(0);
        }
      }
    }
  }

  return orderResult;
}

/**
 * Customer Order Flow
 */
export function customerOrderFlow(accessToken, options = {}) {
  const { enableDynamicOrder = false } = options;

  group('Customer Order Flow', () => {
    // 1. View order history
    testGetMyOrders(accessToken, { page: 0, size: 5 });
    thinkTime(1);

    // 2. Check active orders
    testGetMyActiveOrders(accessToken);
    thinkTime(0.5);

    // 3. Create order (optional)
    if (enableDynamicOrder) {
      const newOrder = testCreateOrderDynamic(accessToken);
      if (newOrder) {
        console.log(`Created dynamic order: ${newOrder.id || newOrder.orderId}`);
      }
    }
  });
}

/**
 * Customer Browse Flow (조회 전용)
 * 주문 생성 없이 조회만 수행
 */
export function customerBrowseFlow(accessToken) {
  group('Customer Browse Flow', () => {
    // 1. 주문 내역 조회
    testGetMyOrders(accessToken, { page: 0, size: 10 });
    thinkTime(1);

    // 2. 활성 주문 조회
    testGetMyActiveOrders(accessToken);
    thinkTime(0.5);
  });
}

/**
 * Customer Integrity Flow (정합성 검증 전용)
 * 주문 생성 + 데이터 정합성 검증
 */
export function customerIntegrityFlow(accessToken) {
  group('Customer Integrity Flow', () => {
    // 동적 주문 생성 + 정합성 검증
    const newOrder = testCreateOrderDynamic(accessToken, { validateIntegrity: true });
    thinkTime(1);

    if (newOrder) {
      console.log(`Integrity test order: ${newOrder.id || newOrder.orderId}`);
    }
  });
}

/**
 * Customer Dynamic Order Flow
 * 동적으로 store/menu를 선택하여 주문하는 전용 플로우
 */
export function customerDynamicOrderFlow(accessToken) {
  group('Customer Dynamic Order Flow', () => {
    // 1. 동적 주문 생성 (store/menu 실시간 조회)
    const newOrder = testCreateOrderDynamic(accessToken);
    thinkTime(1);

    // 2. 주문 내역 확인
    if (newOrder) {
      testGetMyOrders(accessToken, { page: 0, size: 5 });
      thinkTime(0.5);

      testGetMyActiveOrders(accessToken);
    }
  });
}

export default customerOrderFlow;
