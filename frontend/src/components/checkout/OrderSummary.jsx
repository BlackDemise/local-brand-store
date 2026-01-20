import React, { useEffect, useState } from 'react';
import { formatCurrency } from '../../utils/formatters';

/**
 * Order summary with reservation timer
 */
const OrderSummary = ({ cart, reservationExpiry }) => {
  const [timeRemaining, setTimeRemaining] = useState(null);
  
  useEffect(() => {
    if (!reservationExpiry) return;
    
    const updateTimer = () => {
      const now = Date.now();
      const expiry = new Date(reservationExpiry).getTime();
      const remaining = Math.max(0, expiry - now);
      
      setTimeRemaining(remaining);
      
      if (remaining <= 0) {
        // Reservation expired - will be handled by parent component
        return;
      }
    };
    
    // Update immediately
    updateTimer();
    
    // Update every second
    const interval = setInterval(updateTimer, 1000);
    
    return () => clearInterval(interval);
  }, [reservationExpiry]);
  
  const formatTime = (ms) => {
    if (ms === null) return '--:--';
    
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };
  
  const isLowTime = timeRemaining !== null && timeRemaining < 5 * 60 * 1000; // Less than 5 minutes
  const isExpired = timeRemaining === 0;
  
  const itemCount = cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;
  const subtotal = cart?.items?.reduce((sum, item) => sum + (item.price * item.quantity), 0) || 0;
  const shipping = 0; // Free shipping for now
  const total = subtotal + shipping;
  
  return (
    <div className="bg-white rounded-lg shadow-md p-6 sticky top-4">
      <h3 className="text-xl font-bold text-gray-900 mb-4">Đơn hàng của bạn</h3>
      
      {/* Reservation Timer */}
      {reservationExpiry && (
        <div className={`mb-4 p-3 rounded-lg ${
          isExpired 
            ? 'bg-red-100 border border-red-300'
            : isLowTime 
            ? 'bg-yellow-100 border border-yellow-300'
            : 'bg-blue-100 border border-blue-300'
        }`}>
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium">
              {isExpired ? '⏰ Hết thời gian giữ hàng' : '⏰ Thời gian còn lại:'}
            </span>
            <span className={`text-lg font-bold ${
              isExpired 
                ? 'text-red-700'
                : isLowTime 
                ? 'text-yellow-700'
                : 'text-blue-700'
            }`}>
              {formatTime(timeRemaining)}
            </span>
          </div>
          {isLowTime && !isExpired && (
            <p className="text-xs text-yellow-700 mt-1">
              Vui lòng hoàn tất đơn hàng trước khi hết thời gian!
            </p>
          )}
          {isExpired && (
            <p className="text-xs text-red-700 mt-1">
              Vui lòng quay lại giỏ hàng và thử lại.
            </p>
          )}
        </div>
      )}
      
      {/* Items List */}
      <div className="space-y-3 mb-4 max-h-64 overflow-y-auto">
        {cart?.items?.map((item) => (
          <div key={item.id} className="flex justify-between text-sm">
            <div className="flex-1">
              <p className="font-medium text-gray-900">{item.productName}</p>
              <p className="text-gray-600 text-xs">
                {item.size} / {item.color} × {item.quantity}
              </p>
            </div>
            <div className="text-right">
              <p className="font-medium text-gray-900">
                {formatCurrency(item.price * item.quantity)}
              </p>
            </div>
          </div>
        ))}
      </div>
      
      {/* Divider */}
      <div className="border-t border-gray-200 my-4"></div>
      
      {/* Totals */}
      <div className="space-y-2 text-sm">
        <div className="flex justify-between text-gray-600">
          <span>Số lượng:</span>
          <span>{itemCount} sản phẩm</span>
        </div>
        <div className="flex justify-between text-gray-900">
          <span>Tạm tính:</span>
          <span>{formatCurrency(subtotal)}</span>
        </div>
        <div className="flex justify-between text-gray-600">
          <span>Phí vận chuyển:</span>
          <span>{shipping === 0 ? 'Miễn phí' : formatCurrency(shipping)}</span>
        </div>
      </div>
      
      {/* Divider */}
      <div className="border-t border-gray-200 my-4"></div>
      
      {/* Total */}
      <div className="flex justify-between text-lg font-bold">
        <span className="text-gray-900">Tổng cộng:</span>
        <span className="text-blue-600">{formatCurrency(total)}</span>
      </div>
    </div>
  );
};

export default OrderSummary;
