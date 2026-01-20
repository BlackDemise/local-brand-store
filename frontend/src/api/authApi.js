import client from './client';

/**
 * Register new user
 * @param {Object} userData - { email, fullName, password }
 * @returns {Promise<Object>} - Registration response with OTP info
 */
export const register = async (userData) => {
  const response = await client.post('/auth/register', userData);
  return response.data.result;
};

/**
 * Verify OTP after registration
 * @param {string} email - User email
 * @param {string} otpCode - 6-digit OTP code
 * @returns {Promise<Object>} - Access token (refresh token set in cookie)
 */
export const verifyOtp = async (email, otpCode) => {
  const response = await client.post('/auth/verify-otp', { email, otpCode });
  return response.data.result;
};

/**
 * Login user
 * @param {string} email - User email
 * @param {string} password - User password
 * @returns {Promise<Object>} - Access token (refresh token set in cookie)
 */
export const login = async (email, password) => {
  const response = await client.post('/auth/login', { email, password });
  return response.data.result;
};

/**
 * Logout user
 * @returns {Promise<void>}
 */
export const logout = async () => {
  const response = await client.post('/auth/logout');
  return response.data;
};

/**
 * Introspect access token (validate)
 * @returns {Promise<boolean>} - True if token is valid
 */
export const introspect = async () => {
  const response = await client.post('/auth/introspect');
  return response.data.result;
};

/**
 * Refresh access token (uses refresh token cookie)
 * @returns {Promise<Object>} - New access token
 */
export const refresh = async () => {
  const response = await client.post('/auth/refresh');
  return response.data.result;
};
