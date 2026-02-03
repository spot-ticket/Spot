// Authentication Helper Functions
import http from 'k6/http';
import { check } from 'k6';
import { env, endpoints, buildUrl } from '../config/index.js';

// Token storage (shared across VUs via setup data)
let tokenCache = {};

/**
 * Login and get access token
 * @param {string} userType - 'customer', 'owner', 'chef', 'manager', 'master'
 * @returns {object} - { accessToken, refreshToken }
 */
export function login(userType = 'customer') {
  const user = env.users[userType];
  if (!user) {
    console.error(`Unknown user type: ${userType}`);
    return { accessToken: null, refreshToken: null };
  }

  const loginUrl = buildUrl(endpoints.user.login);
  const payload = JSON.stringify({
    username: user.username,
    password: user.password,
  });

  console.log(`Attempting login: ${loginUrl}`);
  console.log(`Username: ${user.username}`);

  const res = http.post(loginUrl, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'login', userType },
  });

  console.log(`Login response: ${res.status}`);

  const success = check(res, {
    'login: status is 200': (r) => r.status === 200,
    'login: has accessToken': (r) => {
      try {
        return JSON.parse(r.body).accessToken !== undefined;
      } catch {
        return false;
      }
    },
  });

  if (!success) {
    console.error(`Login failed for ${userType}!`);
    console.error(`Status: ${res.status}`);
    console.error(`Body: ${res.body}`);
    return { accessToken: null, refreshToken: null };
  }

  const body = JSON.parse(res.body);
  console.log(`Login successful for ${userType}`);

  return {
    accessToken: body.accessToken,
    refreshToken: body.refreshToken,
  };
}

/**
 * Login multiple users for different scenarios
 * @returns {object} - tokens for all user types
 */
export function loginAllUsers() {
  const tokens = {};
  const userTypes = ['customer', 'owner', 'chef', 'manager', 'master'];

  for (const userType of userTypes) {
    tokens[userType] = login(userType);
  }

  return tokens;
}

/**
 * Refresh access token
 * @param {string} refreshToken
 * @returns {object} - { accessToken, refreshToken }
 */
export function refreshToken(refreshToken) {
  const payload = JSON.stringify({ refreshToken });

  const res = http.post(buildUrl(endpoints.user.refresh), payload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'refresh_token' },
  });

  const success = check(res, {
    'refresh: status is 200': (r) => r.status === 200,
  });

  if (!success) {
    console.error(`Token refresh failed! Status: ${res.status}`);
    return { accessToken: null, refreshToken: null };
  }

  const body = JSON.parse(res.body);
  return {
    accessToken: body.accessToken,
    refreshToken: body.refreshToken,
  };
}

/**
 * Get authorization headers
 * @param {string} accessToken
 * @returns {object} - headers object
 */
export function getAuthHeaders(accessToken) {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`,
  };
}

/**
 * Get headers without auth (for public endpoints)
 * @returns {object} - headers object
 */
export function getPublicHeaders() {
  return {
    'Content-Type': 'application/json',
  };
}
