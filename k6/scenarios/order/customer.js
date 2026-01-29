// SPOT-ORDER: Customer Order Scenarios
import http from 'k6/http';
import { check, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { env, endpoints, buildUrl } from '../../config/index.js';
import { getAuthHeaders } from '../../lib/auth.js';
import { thinkTime, buildQueryString } from '../../lib/helpers.js';

// Custom Metrics
const orderCreateDuration = new Trend('order_create_duration');
const orderCreateErrors = new Rate('order_create_errors');
const orderListDuration = new Trend('order_list_duration');
const orderListErrors = new Rate('order_list_errors');
const orderActiveDuration = new Trend('order_active_duration');
const orderCancelDuration = new Trend('order_cancel_duration');

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
 * Customer Order Flow
 */
export function customerOrderFlow(accessToken) {
  group('Customer Order Flow', () => {
    // 1. View order history
    testGetMyOrders(accessToken, { page: 0, size: 5 });
    thinkTime(1);

    // 2. Check active orders
    testGetMyActiveOrders(accessToken);
    thinkTime(0.5);

    // Note: Creating orders might affect data, enable if needed
    // const newOrder = testCreateOrder(accessToken);
  });
}

export default customerOrderFlow;
