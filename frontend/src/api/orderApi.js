import client from './client';

/**
 * Place order
 * @param {Object} orderData - { reservationToken, recipientName, phone, address, paymentMethod, notes }
 * @returns {Promise<Object>} - Placed order with tracking token
 */
export const placeOrder = async (orderData) => {
  const response = await client.post('/order', orderData);
  return response.data.result;
};

/**
 * Track order by tracking token (public endpoint)
 * @param {string} trackingToken - Order tracking token
 * @returns {Promise<Object>} - Order details
 */
export const trackOrder = async (trackingToken) => {
  const response = await client.get(`/order/track/${trackingToken}`);
  return response.data.result;
};

/**
 * Get paginated orders (admin only)
 * @param {Object} filters - { page, size, status, startDate, endDate }
 * @returns {Promise<Object>} - Paginated orders
 */
export const getOrders = async (filters = {}) => {
  const response = await client.get('/order', { params: filters });
  return response.data.result;
};

/**
 * Get order by ID (admin only)
 * @param {number} orderId - Order ID
 * @returns {Promise<Object>} - Order details
 */
export const getOrderById = async (orderId) => {
  const response = await client.get(`/order/${orderId}`);
  return response.data.result;
};

/**
 * Update order status (admin only)
 * @param {number} orderId - Order ID
 * @param {string} newStatus - New status (PROCESSING, SHIPPED, DELIVERED, CANCELLED)
 * @returns {Promise<Object>} - Updated order
 */
export const updateOrderStatus = async (orderId, newStatus) => {
  const response = await client.put(`/order/${orderId}/status`, { newStatus });
  return response.data.result;
};

/**
 * Cancel order (admin only)
 * @param {number} orderId - Order ID
 * @param {string} reason - Cancellation reason
 * @returns {Promise<Object>} - Cancelled order
 */
export const cancelOrder = async (orderId, reason) => {
  const response = await client.post(`/order/${orderId}/cancel`, { reason });
  return response.data.result;
};

/**
 * Get order status history (admin only)
 * @param {number} orderId - Order ID
 * @returns {Promise<Array>} - Order status history
 */
export const getOrderHistory = async (orderId) => {
  const response = await client.get(`/order/${orderId}/history`);
  return response.data.result;
};
