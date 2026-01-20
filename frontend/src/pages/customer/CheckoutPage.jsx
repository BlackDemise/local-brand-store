import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../../hooks/useCart';
import Container from '../../components/layout/Container';
import CheckoutForm from '../../components/checkout/CheckoutForm';
import OrderSummary from '../../components/checkout/OrderSummary';
import Spinner from '../../components/common/Spinner';
import { validatePhone } from '../../utils/validators';
import * as checkoutApi from '../../api/checkoutApi';
import * as orderApi from '../../api/orderApi';

/**
 * Checkout Page - Complete order with shipping and payment
 */
const CheckoutPage = () => {
  const { cart, cartToken, loadCart } = useCart();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(false);
  const [initializing, setInitializing] = useState(true);
  const [reservation, setReservation] = useState(null);
  const [errors, setErrors] = useState({});
  
  const [shippingData, setShippingData] = useState({
    recipientName: '',
    phoneNumber: '',
    address: '',
    notes: '',
  });
  
  const [paymentMethod, setPaymentMethod] = useState('COD');
  
  // Load cart and start checkout on mount
  useEffect(() => {
    const initCheckout = async () => {
      if (!cartToken) {
        navigate('/cart');
        return;
      }
      
      try {
        setInitializing(true);
        
        // Load cart first
        await loadCart();
        
        // Start checkout to create reservation
        const selectedItemIds = cart?.items?.map(item => item.id) || [];
        
        if (selectedItemIds.length === 0) {
          navigate('/cart');
          return;
        }
        
        const reservationData = await checkoutApi.startCheckout(cartToken, selectedItemIds);
        setReservation(reservationData);
        
      } catch (error) {
        console.error('Failed to initialize checkout:', error);
        alert('Không thể bắt đầu thanh toán. Vui lòng thử lại.');
        navigate('/cart');
      } finally {
        setInitializing(false);
      }
    };
    
    initCheckout();
  }, [cartToken]);
  
  // Check if reservation expired
  useEffect(() => {
    if (!reservation?.expiresAt) return;
    
    const checkExpiry = () => {
      const now = Date.now();
      const expiry = new Date(reservation.expiresAt).getTime();
      
      if (now >= expiry) {
        alert('Thời gian giữ hàng đã hết. Vui lòng thử lại.');
        navigate('/cart');
      }
    };
    
    // Check every 10 seconds
    const interval = setInterval(checkExpiry, 10000);
    
    return () => clearInterval(interval);
  }, [reservation]);
  
  // Validate form
  const validateForm = () => {
    const newErrors = {};
    
    if (!shippingData.recipientName?.trim()) {
      newErrors.recipientName = 'Vui lòng nhập tên người nhận';
    }
    
    if (!shippingData.phoneNumber?.trim()) {
      newErrors.phoneNumber = 'Vui lòng nhập số điện thoại';
    } else if (!validatePhone(shippingData.phoneNumber)) {
      newErrors.phoneNumber = 'Số điện thoại không hợp lệ';
    }
    
    if (!shippingData.address?.trim()) {
      newErrors.address = 'Vui lòng nhập địa chỉ giao hàng';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  // Handle order submission
  const handlePlaceOrder = async () => {
    if (!validateForm()) {
      return;
    }
    
    // Check reservation expiry
    if (reservation) {
      const now = Date.now();
      const expiry = new Date(reservation.expiresAt).getTime();
      
      if (now >= expiry) {
        alert('Thời gian giữ hàng đã hết. Vui lòng thử lại.');
        navigate('/cart');
        return;
      }
    }
    
    setLoading(true);
    
    try {
      const orderData = {
        cartToken,
        recipientName: shippingData.recipientName.trim(),
        phoneNumber: shippingData.phoneNumber.trim(),
        address: shippingData.address.trim(),
        notes: shippingData.notes?.trim() || null,
        paymentMethod,
      };
      
      const order = await orderApi.placeOrder(orderData);
      
      // Navigate to confirmation page with order data
      navigate('/order-confirmation', { 
        state: { order },
        replace: true
      });
      
    } catch (error) {
      console.error('Failed to place order:', error);
      
      const errorMessage = error.response?.data?.message || 'Đã có lỗi xảy ra. Vui lòng thử lại.';
      alert(errorMessage);
      
      // If reservation expired or cart changed, redirect to cart
      if (error.response?.status === 400 || error.response?.status === 409) {
        navigate('/cart');
      }
    } finally {
      setLoading(false);
    }
  };
  
  // Loading state
  if (initializing) {
    return (
      <Container className="py-8">
        <div className="flex justify-center items-center h-64">
          <Spinner size="large" />
          <p className="ml-3 text-gray-600">Đang khởi tạo thanh toán...</p>
        </div>
      </Container>
    );
  }
  
  // No cart or reservation
  if (!cart || !reservation) {
    return null;
  }
  
  const isExpired = reservation && new Date(reservation.expiresAt).getTime() <= Date.now();
  
  return (
    <Container className="py-8">
      {/* Breadcrumb */}
      <nav className="text-sm text-gray-600 mb-6">
        <a href="/" className="hover:text-blue-600">Trang chủ</a>
        <span className="mx-2">/</span>
        <a href="/cart" className="hover:text-blue-600">Giỏ hàng</a>
        <span className="mx-2">/</span>
        <span className="text-gray-900">Thanh toán</span>
      </nav>
      
      {/* Page Title */}
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Thanh toán</h1>
      
      {/* Checkout Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Checkout Form */}
        <div className="lg:col-span-2">
          <CheckoutForm
            shippingData={shippingData}
            setShippingData={setShippingData}
            paymentMethod={paymentMethod}
            setPaymentMethod={setPaymentMethod}
            errors={errors}
            onSubmit={handlePlaceOrder}
            loading={loading}
            disabled={isExpired}
          />
          
          {/* Back to Cart Link */}
          <div className="mt-4 text-center">
            <button
              onClick={() => navigate('/cart')}
              className="text-blue-600 hover:text-blue-800 text-sm"
              disabled={loading}
            >
              ← Quay lại giỏ hàng
            </button>
          </div>
        </div>
        
        {/* Order Summary */}
        <div className="lg:col-span-1">
          <OrderSummary
            cart={cart}
            reservationExpiry={reservation?.expiresAt}
          />
        </div>
      </div>
    </Container>
  );
};

export default CheckoutPage;
