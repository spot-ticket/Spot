// Performance Thresholds Configuration

export const thresholds = {
  // Response Time (ms)
  responseTime: {
    fast: 200,      // 빠른 응답 (단순 조회)
    normal: 500,    // 일반 응답
    slow: 1000,     // 느린 응답 (복잡한 연산)
    timeout: 3000,  // 타임아웃
  },

  // Percentile Targets
  percentiles: {
    p50: 200,   // 50% 요청
    p90: 400,   // 90% 요청
    p95: 500,   // 95% 요청
    p99: 1000,  // 99% 요청
  },

  // Error Rate
  errorRate: {
    acceptable: 0.01,  // 1% 미만
    warning: 0.05,     // 5% 미만
    critical: 0.1,     // 10% 미만
  },

  // Throughput (requests per second)
  throughput: {
    minimum: 10,
    target: 100,
    peak: 500,
  },
};

// K6 Threshold Configurations
export const k6Thresholds = {
  // Global thresholds
  global: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.1'],
  },

  // Service-specific thresholds
  user: {
    'user_login_duration': ['p(95)<300'],
    'user_join_duration': ['p(95)<500'],
    'user_get_duration': ['p(95)<200'],
  },

  store: {
    'store_list_duration': ['p(95)<500'],
    'store_detail_duration': ['p(95)<300'],
    'store_search_duration': ['p(95)<500'],
    'menu_list_duration': ['p(95)<300'],
    'menu_detail_duration': ['p(95)<200'],
    'category_list_duration': ['p(95)<200'],
  },

  order: {
    'order_create_duration': ['p(95)<1000'],
    'order_list_duration': ['p(95)<500'],
    'order_accept_duration': ['p(95)<500'],
    'order_complete_duration': ['p(95)<500'],
  },

  payment: {
    'payment_confirm_duration': ['p(95)<2000'],
    'payment_cancel_duration': ['p(95)<1000'],
    'payment_list_duration': ['p(95)<500'],
  },
};

// Test Scenarios Configuration
export const scenarios = {
  // Smoke Test: 빠른 검증
  smoke: {
    vus: 1,
    duration: '30s',
  },

  // Load Test: 일반 부하
  load: {
    stages: [
      { duration: '1m', target: 10 },   // ramp up
      { duration: '3m', target: 10 },   // steady
      { duration: '1m', target: 0 },    // ramp down
    ],
  },

  // Stress Test: 스트레스 테스트
  stress: {
    stages: [
      { duration: '1m', target: 10 },
      { duration: '2m', target: 20 },
      { duration: '2m', target: 50 },
      { duration: '2m', target: 100 },
      { duration: '3m', target: 100 },
      { duration: '2m', target: 0 },
    ],
  },

  // Spike Test: 급격한 부하
  spike: {
    stages: [
      { duration: '30s', target: 5 },
      { duration: '10s', target: 100 },  // spike!
      { duration: '1m', target: 100 },
      { duration: '10s', target: 5 },
      { duration: '30s', target: 0 },
    ],
  },

  // Soak Test: 장시간 부하
  soak: {
    stages: [
      { duration: '2m', target: 20 },
      { duration: '30m', target: 20 },
      { duration: '2m', target: 0 },
    ],
  },
};
