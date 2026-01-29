// API Endpoints Configuration
// Spot MSA 서비스별 API 엔드포인트 정의

export const endpoints = {
  // ==================== SPOT-USER (8081) ====================
  user: {
    // Auth
    login: '/api/login',
    join: '/api/join',
    refresh: '/api/auth/refresh',
    logout: '/api/auth/logout',

    // User Management
    getUser: (userId) => `/api/users/${userId}`,
    updateUser: (userId) => `/api/users/${userId}`,
    deleteMe: '/api/users/me',
    searchUsers: '/api/users/search',

    // Admin - Users
    adminGetUsers: '/api/admin/users',
    adminUpdateUserRole: (userId) => `/api/admin/users/${userId}/role`,
    adminDeleteUser: (userId) => `/api/admin/users/${userId}`,

    // Admin - Stores
    adminGetStores: '/api/admin/stores',
    adminApproveStore: (storeId) => `/api/admin/stores/${storeId}/approve`,
    adminDeleteStore: (storeId) => `/api/admin/stores/${storeId}`,

    // Admin - Stats
    adminStats: '/api/admin/stats',
  },

  // ==================== SPOT-STORE (8083) ====================
  store: {
    // Store Management
    createStore: '/api/stores',
    getMyStore: '/api/stores/my',
    getStore: (storeId) => `/api/stores/${storeId}`,
    getStores: '/api/stores',
    updateStore: (storeId) => `/api/stores/${storeId}`,
    updateStoreStaff: (storeId) => `/api/stores/${storeId}/staff`,
    deleteStore: (storeId) => `/api/stores/${storeId}`,
    updateStoreStatus: (storeId) => `/api/stores/${storeId}/status`,
    searchStores: '/api/stores/search',

    // Category Management
    getCategories: '/api/categories',
    getStoresByCategory: (categoryName) => `/api/categories/${categoryName}/stores`,
    createCategory: '/api/categories',
    updateCategory: (categoryId) => `/api/categories/${categoryId}`,
    deleteCategory: (categoryId) => `/api/categories/${categoryId}`,

    // Menu Management
    getMenus: (storeId) => `/api/stores/${storeId}/menus`,
    getMenu: (storeId, menuId) => `/api/stores/${storeId}/menus/${menuId}`,
    createMenu: (storeId) => `/api/stores/${storeId}/menus`,
    updateMenu: (storeId, menuId) => `/api/stores/${storeId}/menus/${menuId}`,
    deleteMenu: (storeId, menuId) => `/api/stores/${storeId}/menus/${menuId}`,
    hideMenu: (storeId, menuId) => `/api/stores/${storeId}/menus/${menuId}/hide`,

    // Menu Option Management
    createMenuOption: (storeId, menuId) => `/api/stores/${storeId}/menus/${menuId}/options`,
    updateMenuOption: (storeId, menuId, optionId) => `/api/stores/${storeId}/menus/${menuId}/options/${optionId}`,
    deleteMenuOption: (storeId, menuId, optionId) => `/api/stores/${storeId}/menus/${menuId}/options/${optionId}`,
    hideMenuOption: (storeId, menuId, optionId) => `/api/stores/${storeId}/menus/${menuId}/options/${optionId}/hide`,

    // Review Management
    createReview: '/api/reviews',
    getStoreReviews: (storeId) => `/api/reviews/stores/${storeId}`,
    getStoreReviewStats: (storeId) => `/api/reviews/stores/${storeId}/stats`,
    updateReview: (reviewId) => `/api/reviews/${reviewId}`,
    deleteReview: (reviewId) => `/api/reviews/${reviewId}`,
  },

  // ==================== SPOT-ORDER (8082) ====================
  order: {
    // Customer Orders
    createOrder: '/api/orders',
    getMyOrders: '/api/orders/my',
    getMyActiveOrders: '/api/orders/my/active',
    customerCancelOrder: (orderId) => `/api/orders/${orderId}/customer-cancel`,

    // Owner Orders
    getMyStoreOrders: '/api/orders/my-store',
    getMyStoreActiveOrders: '/api/orders/my-store/active',
    acceptOrder: (orderId) => `/api/orders/${orderId}/accept`,
    rejectOrder: (orderId) => `/api/orders/${orderId}/reject`,
    completeOrder: (orderId) => `/api/orders/${orderId}/complete`,
    storeCancelOrder: (orderId) => `/api/orders/${orderId}/store-cancel`,

    // Chef Orders
    getChefTodayOrders: '/api/orders/chef/today',
    startCooking: (orderId) => `/api/orders/${orderId}/start-cooking`,
    readyOrder: (orderId) => `/api/orders/${orderId}/ready`,

    // Admin Orders
    adminGetOrders: '/api/admin/orders',
    adminGetOrderStats: '/api/admin/orders/stats',
  },

  // ==================== SPOT-PAYMENT (8084) ====================
  payment: {
    confirmPayment: (orderId) => `/api/payments/${orderId}/confirm`,
    cancelPayment: (orderId) => `/api/payments/${orderId}/cancel`,
    getPayments: '/api/payments',
    getPayment: (paymentId) => `/api/payments/${paymentId}`,
    getCancelledPayments: '/api/payments/cancel',
    getCancelledPayment: (paymentId) => `/api/payments/${paymentId}/cancel`,
    saveBillingKey: '/api/payments/billing-key',
    checkBillingKeyExists: '/api/payments/billing-key/exists',
    savePaymentHistory: '/api/payments/history',
  },
};
