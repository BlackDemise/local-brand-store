import React, { useEffect, useState } from 'react';
import { useCart } from '../../hooks/useCart';
import Container from '../../components/layout/Container';
import CartItem from '../../components/cart/CartItem';
import CartSummary from '../../components/cart/CartSummary';
import StockWarning from '../../components/cart/StockWarning';
import Spinner from '../../components/common/Spinner';
import Button from '../../components/common/Button';
import { useNavigate } from 'react-router-dom';

/**
 * Cart Page - Display cart items and summary
 */
const CartPage = () => {
  const { cart, loading, loadCart, updateQuantity, removeItem, clearCart, cartToken } = useCart();
  const navigate = useNavigate();
  const [stockWarnings, setStockWarnings] = useState([]);
  
  useEffect(() => {
    // Load cart when page mounts
    if (cartToken) {
      loadCart();
    }
  }, [cartToken]);
  
  useEffect(() => {
    // Check for stock warnings
    if (cart?.items) {
      const warnings = cart.items
        .filter(item => item.quantity > item.availableStock)
        .map(item => 
          `${item.productName} (${item.size}, ${item.color}): Chỉ còn ${item.availableStock} sản phẩm nhưng bạn đang chọn ${item.quantity}`
        );
      setStockWarnings(warnings);
    }
  }, [cart]);
  
  const handleUpdateQuantity = async (cartItemId, quantity) => {
    try {
      await updateQuantity(cartItemId, quantity);
    } catch (error) {
      console.error('Failed to update quantity:', error);
      // Toast notification would go here
    }
  };
  
  const handleRemoveItem = async (cartItemId) => {
    if (!window.confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?')) {
      return;
    }
    
    try {
      await removeItem(cartItemId);
    } catch (error) {
      console.error('Failed to remove item:', error);
      // Toast notification would go here
    }
  };
  
  const handleClearCart = async () => {
    if (!window.confirm('Bạn có chắc muốn xóa tất cả sản phẩm khỏi giỏ hàng?')) {
      return;
    }
    
    try {
      await clearCart();
    } catch (error) {
      console.error('Failed to clear cart:', error);
      // Toast notification would go here
    }
  };
  
  // Loading state
  if (loading && !cart) {
    return (
      <Container className="py-8">
        <div className="flex justify-center items-center h-64">
          <Spinner size="large" />
        </div>
      </Container>
    );
  }
  
  // Empty cart state
  if (!cart?.items || cart.items.length === 0) {
    return (
      <Container className="py-12">
        <div className="text-center">
          <svg
            className="mx-auto h-24 w-24 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1}
              d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
            />
          </svg>
          <h2 className="mt-4 text-2xl font-bold text-gray-900">
            Giỏ hàng trống
          </h2>
          <p className="mt-2 text-gray-600">
            Bạn chưa có sản phẩm nào trong giỏ hàng
          </p>
          <Button
            onClick={() => navigate('/products')}
            variant="primary"
            className="mt-6"
          >
            Bắt đầu mua sắm
          </Button>
        </div>
      </Container>
    );
  }
  
  const hasStockWarnings = stockWarnings.length > 0;
  
  return (
    <Container className="py-8">
      {/* Breadcrumb */}
      <nav className="text-sm text-gray-600 mb-6">
        <a href="/" className="hover:text-blue-600">Trang chủ</a>
        <span className="mx-2">/</span>
        <span className="text-gray-900">Giỏ hàng</span>
      </nav>
      
      {/* Page Title */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">
          Giỏ hàng của bạn
        </h1>
        {cart.items.length > 0 && (
          <button
            onClick={handleClearCart}
            disabled={loading}
            className="text-red-500 hover:text-red-700 text-sm disabled:opacity-50"
          >
            Xóa tất cả
          </button>
        )}
      </div>
      
      {/* Stock Warnings */}
      {hasStockWarnings && <StockWarning warnings={stockWarnings} />}
      
      {/* Cart Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item) => (
            <CartItem
              key={item.id}
              item={item}
              onUpdateQuantity={handleUpdateQuantity}
              onRemove={handleRemoveItem}
              disabled={loading}
            />
          ))}
        </div>
        
        {/* Cart Summary */}
        <div className="lg:col-span-1">
          <CartSummary
            cart={cart}
            hasStockWarnings={hasStockWarnings}
            loading={loading}
          />
        </div>
      </div>
    </Container>
  );
};

export default CartPage;
