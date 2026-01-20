import client from './client';

/**
 * Get cart by token
 * @param {string} cartToken - Cart token from localStorage
 * @returns {Promise<Object>} - Cart with items
 */
export const getCart = async (cartToken) => {
  const response = await client.get('/cart', {
    headers: { 'X-Cart-Token': cartToken },
  });
  return response.data.result;
};

/**
 * Add item to cart
 * @param {string} cartToken - Cart token
 * @param {number} skuId - SKU ID
 * @param {number} quantity - Quantity to add
 * @returns {Promise<Object>} - Updated cart
 */
export const addToCart = async (cartToken, skuId, quantity) => {
  const response = await client.post(
    '/cart/items',
    { skuId, quantity },
    { headers: { 'X-Cart-Token': cartToken } }
  );
  return response.data.result;
};

/**
 * Update cart item quantity
 * @param {string} cartToken - Cart token
 * @param {number} cartItemId - Cart item ID
 * @param {number} quantity - New quantity
 * @returns {Promise<Object>} - Updated cart
 */
export const updateCartItem = async (cartToken, cartItemId, quantity) => {
  const response = await client.put(
    `/cart/items/${cartItemId}`,
    { quantity },
    { headers: { 'X-Cart-Token': cartToken } }
  );
  return response.data.result;
};

/**
 * Remove item from cart
 * @param {string} cartToken - Cart token
 * @param {number} cartItemId - Cart item ID
 * @returns {Promise<Object>} - Updated cart
 */
export const removeCartItem = async (cartToken, cartItemId) => {
  const response = await client.delete(`/cart/items/${cartItemId}`, {
    headers: { 'X-Cart-Token': cartToken },
  });
  return response.data.result;
};

/**
 * Clear all items from cart
 * @param {string} cartToken - Cart token
 * @returns {Promise<Object>} - Empty cart
 */
export const clearCart = async (cartToken) => {
  const response = await client.delete('/cart', {
    headers: { 'X-Cart-Token': cartToken },
  });
  return response.data.result;
};
