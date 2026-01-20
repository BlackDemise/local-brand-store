import React, { useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import Container from '../../components/layout/Container';
import Button from '../../components/common/Button';
import OrderStatus from '../../components/order/OrderStatus';
import OrderDetails from '../../components/order/OrderDetails';

/**
 * Order Confirmation Page - Display after successful order placement
 */
const OrderConfirmationPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const order = location.state?.order;
  
  useEffect(() => {
    // If no order data, redirect to home
    if (!order) {
      navigate('/', { replace: true });
    }
  }, [order, navigate]);
  
  if (!order) {
    return null;
  }

  return (
    <Container className="py-12">
      <div className="max-w-3xl mx-auto">
        {/* Success Icon */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-100 mb-4">
            <svg
              className="w-12 h-12 text-green-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5 13l4 4L19 7"
              />
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Đặt hàng thành công!
          </h1>
          <p className="text-gray-600">
            Cảm ơn bạn đã đặt hàng. Chúng tôi đã nhận được đơn hàng của bạn.
          </p>
        </div>
        
        {/* Order Status Badge */}
        <div className="flex justify-center mb-6">
          <OrderStatus status={order.status} size="lg" showIcon={true} />
        </div>
        
        {/* Tracking Token Card */}
        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg p-6 mb-6">
          <div className="text-center">
            <p className="text-sm text-blue-900 mb-3 font-medium">
              Mã tra cứu đơn hàng của bạn
            </p>
            <div className="flex items-center justify-center gap-3">
              <code className="text-2xl font-mono font-bold text-blue-700">
                {order.trackingToken}
              </code>
              <button
                onClick={() => {
                  navigator.clipboard.writeText(order.trackingToken);
                  alert('Đã sao chép mã tra cứu!');
                }}
                className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-100 rounded-md transition-colors"
                title="Sao chép mã"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
              </button>
            </div>
            <p className="text-xs text-blue-700 mt-3">
              Sử dụng mã này để tra cứu trạng thái đơn hàng bất kỳ lúc nào
            </p>
          </div>
        </div>
        
        {/* Order Details Component */}
        <div className="mb-6">
          <OrderDetails order={order} />
        </div>
        
        {/* Email Confirmation Notice */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <div className="flex">
            <svg
              className="w-5 h-5 text-blue-600 mt-0.5 mr-3 flex-shrink-0"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
              <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
            </svg>
            <div className="text-sm text-blue-900">
              <p className="font-medium mb-1">Email xác nhận đã được gửi</p>
              <p>
                Chúng tôi đã gửi email xác nhận đơn hàng đến địa chỉ email của bạn.
                Vui lòng kiểm tra hộp thư để biết thêm chi tiết.
              </p>
            </div>
          </div>
        </div>
        
        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4">
          <Button
            onClick={() => navigate(`/track/${order.trackingToken}`)}
            variant="primary"
            className="flex-1"
          >
            Tra cứu đơn hàng
          </Button>
          <Button
            onClick={() => navigate('/products')}
            variant="outline"
            className="flex-1"
          >
            Tiếp tục mua sắm
          </Button>
        </div>
      </div>
    </Container>
  );
};

export default OrderConfirmationPage;
