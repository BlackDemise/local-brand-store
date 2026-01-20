import React from 'react';
import { useNavigate } from 'react-router-dom';
import { formatCurrency } from '../../utils/formatters';
import Button from '../common/Button';

/**
 * Cart summary with totals and checkout button
 */
const CartSummary = ({ cart, hasStockWarnings = false, loading = false }) => {
  const navigate = useNavigate();
  
  const itemCount = cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;
  const subtotal = cart?.items?.reduce((sum, item) => sum + (item.price * item.quantity), 0) || 0;
  
  // In Phase 3, shipping is not calculated yet
  const shipping = 0;
  const total = subtotal + shipping;
  
  const canCheckout = itemCount > 0 && !hasStockWarnings && !loading;
  
  const handleCheckout = () => {
    if (canCheckout) {
      navigate('/checkout');
    }
  };
  
  return (
    <div className="bg-white rounded-lg shadow-md p-6 sticky top-4">
      <h2 className="text-xl font-bold text-gray-900 mb-4">Tóm tắt đơn hàng</h2>
      
      {/* Item Count */}
      <div className="flex justify-between text-gray-600 mb-2">
        <span>Số lượng sản phẩm:</span>
        <span className="font-semibold">{itemCount}</span>
      </div>
      
      {/* Subtotal */}
      <div className="flex justify-between text-gray-900 mb-2">
        <span>Tạm tính:</span>
        <span className="font-semibold">{formatCurrency(subtotal)}</span>
      </div>
      
      {/* Shipping */}
      <div className="flex justify-between text-gray-600 mb-4">
        <span>Phí vận chuyển:</span>
        <span>{shipping === 0 ? 'Miễn phí' : formatCurrency(shipping)}</span>
      </div>
      
      {/* Divider */}
      <div className="border-t border-gray-200 my-4"></div>
      
      {/* Total */}
      <div className="flex justify-between text-lg font-bold text-gray-900 mb-6">
        <span>Tổng cộng:</span>
        <span className="text-blue-600">{formatCurrency(total)}</span>
      </div>
      
      {/* Warnings */}
      {hasStockWarnings && (
        <div className="mb-4 text-sm text-yellow-600 bg-yellow-50 p-3 rounded">
          ⚠️ Vui lòng điều chỉnh số lượng sản phẩm trước khi thanh toán
        </div>
      )}
      
      {/* Checkout Button */}
      <Button
        onClick={handleCheckout}
        disabled={!canCheckout}
        variant="primary"
        className="w-full mb-3"
      >
        {loading ? 'Đang xử lý...' : 'Tiến hành thanh toán'}
      </Button>
      
      {/* Continue Shopping Link */}
      <button
        onClick={() => navigate('/products')}
        className="w-full text-center text-blue-600 hover:text-blue-800 text-sm"
      >
        ← Tiếp tục mua sắm
      </button>
      
      {/* Additional Info */}
      <div className="mt-6 text-xs text-gray-500 space-y-1">
        <p>✓ Miễn phí vận chuyển cho đơn hàng</p>
        <p>✓ Đổi trả trong vòng 7 ngày</p>
        <p>✓ Thanh toán an toàn</p>
      </div>
    </div>
  );
};

export default CartSummary;
