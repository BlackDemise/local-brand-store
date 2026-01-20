import { useState, useCallback } from 'react';
import * as orderApi from '../api/orderApi';

/**
 * Custom hook for order operations
 * Handles order placement and tracking with loading and error states
 */
const useOrder = () => {
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Place a new order
   * @param {Object} orderData - Order data including shipping info, items, payment method
   * @returns {Promise<Object>} Created order
   */
  const placeOrder = useCallback(async (orderData) => {
    setLoading(true);
    setError(null);
    
    try {
      const createdOrder = await orderApi.placeOrder(orderData);
      setOrder(createdOrder);
      return createdOrder;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Không thể đặt hàng. Vui lòng thử lại.';
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Track an order by tracking token
   * @param {string} trackingToken - Order tracking token
   * @returns {Promise<Object>} Order details
   */
  const trackOrder = useCallback(async (trackingToken) => {
    if (!trackingToken || !trackingToken.trim()) {
      const errorMessage = 'Vui lòng nhập mã tra cứu đơn hàng';
      setError(errorMessage);
      throw new Error(errorMessage);
    }

    setLoading(true);
    setError(null);
    
    try {
      const orderData = await orderApi.trackOrder(trackingToken.trim());
      setOrder(orderData);
      return orderData;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Không tìm thấy đơn hàng. Vui lòng kiểm tra lại mã tra cứu.';
      setError(errorMessage);
      setOrder(null);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Clear current order and error state
   */
  const clearOrder = useCallback(() => {
    setOrder(null);
    setError(null);
  }, []);

  /**
   * Reset error state
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    order,
    loading,
    error,
    placeOrder,
    trackOrder,
    clearOrder,
    clearError,
  };
};

export default useOrder;
