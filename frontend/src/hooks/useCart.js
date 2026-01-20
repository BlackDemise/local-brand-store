import { useCartContext } from '../context/CartContext';

/**
 * Custom hook for cart operations
 * Re-exports cart context for easy access
 */
export const useCart = () => {
  return useCartContext();
};
