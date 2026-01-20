import client from './client';

/**
 * Start checkout process
 * @param {string} cartToken - Cart token
 * @param {Array<number>} selectedItemIds - Cart item IDs to checkout
 * @returns {Promise<Object>} - Checkout response with reservation and summary
 */
export const startCheckout = async (cartToken, selectedItemIds) => {
  const response = await client.post(
    '/checkout/start',
    { selectedItemIds },
    { headers: { 'X-Cart-Token': cartToken } }
  );
  return response.data.result;
};
