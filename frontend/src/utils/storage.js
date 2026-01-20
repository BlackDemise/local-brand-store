import { STORAGE_KEYS } from './constants';

/**
 * Get cart token from localStorage
 * @returns {string|null} - Cart token or null
 */
export const getCartToken = () => {
  return localStorage.getItem(STORAGE_KEYS.CART_TOKEN);
};

/**
 * Set cart token in localStorage
 * @param {string} token - Cart token to save
 */
export const setCartToken = (token) => {
  if (token) {
    localStorage.setItem(STORAGE_KEYS.CART_TOKEN, token);
  } else {
    localStorage.removeItem(STORAGE_KEYS.CART_TOKEN);
  }
};

/**
 * Get access token from localStorage
 * @returns {string|null} - Access token or null
 */
export const getAccessToken = () => {
  return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
};

/**
 * Set access token in localStorage
 * @param {string} token - Access token to save
 */
export const setAccessToken = (token) => {
  if (token) {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token);
  } else {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
  }
};

/**
 * Get user info from localStorage
 * @returns {Object|null} - User info object or null
 */
export const getUserInfo = () => {
  const userInfo = localStorage.getItem(STORAGE_KEYS.USER_INFO);
  return userInfo ? JSON.parse(userInfo) : null;
};

/**
 * Set user info in localStorage
 * @param {Object} user - User info object
 */
export const setUserInfo = (user) => {
  if (user) {
    localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(user));
  } else {
    localStorage.removeItem(STORAGE_KEYS.USER_INFO);
  }
};

/**
 * Clear all authentication data
 */
export const clearAuthData = () => {
  localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
  localStorage.removeItem(STORAGE_KEYS.USER_INFO);
};

/**
 * Clear all stored data
 */
export const clearStorage = () => {
  localStorage.clear();
};

/**
 * Generic localStorage getter
 * @param {string} key - Storage key
 * @param {any} defaultValue - Default value if key not found
 * @returns {any} - Stored value or default
 */
export const getStorageItem = (key, defaultValue = null) => {
  try {
    const item = localStorage.getItem(key);
    return item ? JSON.parse(item) : defaultValue;
  } catch (error) {
    console.error(`Error reading from localStorage: ${key}`, error);
    return defaultValue;
  }
};

/**
 * Generic localStorage setter
 * @param {string} key - Storage key
 * @param {any} value - Value to store
 */
export const setStorageItem = (key, value) => {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (error) {
    console.error(`Error writing to localStorage: ${key}`, error);
  }
};

/**
 * Remove item from localStorage
 * @param {string} key - Storage key
 */
export const removeStorageItem = (key) => {
  try {
    localStorage.removeItem(key);
  } catch (error) {
    console.error(`Error removing from localStorage: ${key}`, error);
  }
};
