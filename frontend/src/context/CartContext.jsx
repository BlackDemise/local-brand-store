import React, { createContext, useContext, useState, useEffect } from 'react';
import { getCartToken, setCartToken as saveCartToken } from '../utils/storage';
import * as cartApi from '../api/cartApi';
import { v4 as uuidv4 } from 'uuid';

const CartContext = createContext();

export const useCartContext = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCartContext must be used within CartProvider');
  }
  return context;
};

/**
 * Cart Context Provider
 * Manages cart state and operations
 */
export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [cartToken, setCartTokenState] = useState(() => getCartToken());
  
  // Initialize cart token if not exists
  useEffect(() => {
    if (!cartToken) {
      const newToken = uuidv4();
      setCartTokenState(newToken);
      saveCartToken(newToken);
    }
  }, [cartToken]);
  
  // Note: We don't auto-load cart on mount because it might not exist yet
  // Cart will be loaded after first operation (addToCart, etc.)
  
  /**
   * Load cart from API
   */
  const loadCart = async () => {
    if (!cartToken) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const data = await cartApi.getCart(cartToken);
      setCart(data);
    } catch (err) {
      // Handle 404/400 - cart doesn't exist yet (first time user)
      if (err.response?.status === 404 || err.response?.status === 400) {
        setCart({ items: [] }); // Initialize empty cart
        setError(null); // Don't show error for missing cart
      } else {
        setError(err.message);
        console.error('Failed to load cart:', err);
      }
    } finally {
      setLoading(false);
    }
  };
  
  /**
   * Add item to cart
   */
  const addToCart = async (skuId, quantity = 1) => {
    if (!cartToken) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const data = await cartApi.addToCart(cartToken, skuId, quantity);
      setCart(data);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  /**
   * Update cart item quantity
   */
  const updateQuantity = async (cartItemId, quantity) => {
    if (!cartToken) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const data = await cartApi.updateCartItem(cartToken, cartItemId, quantity);
      setCart(data);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  /**
   * Remove item from cart
   */
  const removeItem = async (cartItemId) => {
    if (!cartToken) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const data = await cartApi.removeCartItem(cartToken, cartItemId);
      setCart(data);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  /**
   * Clear all items from cart
   */
  const clearCart = async () => {
    if (!cartToken) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const data = await cartApi.clearCart(cartToken);
      setCart(data);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  /**
   * Refresh cart data (call this when navigating to cart page)
   */
  const refreshCart = async () => {
    await loadCart();
  };
  
  /**
   * Calculate total item count
   */
  const itemCount = cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;
  
  /**
   * Calculate subtotal
   */
  const subtotal = cart?.items?.reduce((sum, item) => sum + (item.price * item.quantity), 0) || 0;
  
  const value = {
    cart,
    cartToken,
    loading,
    itemCount,
    subtotal,
    addToCart,
    updateQuantity,
    removeItem,
    clearCart,
    loadCart,
    refreshCart,
  };
  
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};
