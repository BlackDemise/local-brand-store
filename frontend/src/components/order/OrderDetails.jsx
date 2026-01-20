import React from 'react';
import PropTypes from 'prop-types';
import { formatCurrency, formatDateTime } from '../../utils/formatters';

/**
 * OrderDetails Component
 * Displays comprehensive order information including customer details and order items
 */
const OrderDetails = ({ order }) => {
  if (!order) {
    return null;
  }

  const getPaymentMethodText = (method) => {
    switch (method) {
      case 'COD':
        return 'Thanh toán khi nhận hàng (COD)';
      case 'BANK_TRANSFER':
        return 'Chuyển khoản ngân hàng';
      case 'E_WALLET':
        return 'Ví điện tử';
      default:
        return method;
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      {/* Order Header */}
      <div className="border-b border-gray-200 pb-4 mb-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          Chi tiết đơn hàng
        </h2>
        <div className="flex flex-wrap gap-4 text-sm text-gray-600">
          <div>
            <span className="font-medium">Mã đơn hàng:</span>{' '}
            <span className="font-mono">{order.orderNumber}</span>
          </div>
          <div>
            <span className="font-medium">Ngày đặt:</span>{' '}
            {formatDateTime(order.createdAt)}
          </div>
          {order.trackingToken && (
            <div>
              <span className="font-medium">Mã tra cứu:</span>{' '}
              <span className="font-mono">{order.trackingToken}</span>
            </div>
          )}
        </div>
      </div>

      {/* Customer Information */}
      <div className="mb-6">
        <h3 className="font-semibold text-gray-900 mb-3">Thông tin khách hàng</h3>
        <div className="bg-gray-50 rounded-lg p-4 space-y-2 text-sm">
          <div className="flex">
            <span className="font-medium text-gray-700 w-32">Người nhận:</span>
            <span className="text-gray-900">{order.recipientName}</span>
          </div>
          <div className="flex">
            <span className="font-medium text-gray-700 w-32">Số điện thoại:</span>
            <span className="text-gray-900">{order.phoneNumber}</span>
          </div>
          <div className="flex">
            <span className="font-medium text-gray-700 w-32">Địa chỉ:</span>
            <span className="text-gray-900">{order.address}</span>
          </div>
          {order.email && (
            <div className="flex">
              <span className="font-medium text-gray-700 w-32">Email:</span>
              <span className="text-gray-900">{order.email}</span>
            </div>
          )}
          {order.notes && (
            <div className="flex">
              <span className="font-medium text-gray-700 w-32">Ghi chú:</span>
              <span className="text-gray-900">{order.notes}</span>
            </div>
          )}
        </div>
      </div>

      {/* Payment Information */}
      <div className="mb-6">
        <h3 className="font-semibold text-gray-900 mb-3">Phương thức thanh toán</h3>
        <div className="bg-gray-50 rounded-lg p-4 text-sm">
          <span className="text-gray-900">{getPaymentMethodText(order.paymentMethod)}</span>
        </div>
      </div>

      {/* Order Items */}
      <div className="mb-6">
        <h3 className="font-semibold text-gray-900 mb-3">Sản phẩm</h3>
        <div className="border border-gray-200 rounded-lg overflow-hidden">
          <div className="divide-y divide-gray-200">
            {order.items?.map((item, index) => (
              <div key={index} className="p-4 hover:bg-gray-50 transition-colors">
                <div className="flex justify-between items-start gap-4">
                  <div className="flex-1">
                    <h4 className="font-medium text-gray-900 mb-1">
                      {item.productName}
                    </h4>
                    <div className="flex flex-wrap gap-2 text-xs text-gray-600">
                      <span className="bg-gray-100 px-2 py-1 rounded">
                        Size: {item.size}
                      </span>
                      <span className="bg-gray-100 px-2 py-1 rounded">
                        Màu: {item.color}
                      </span>
                      <span className="bg-gray-100 px-2 py-1 rounded">
                        SL: {item.quantity}
                      </span>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-gray-900 mb-1">
                      {formatCurrency(item.price * item.quantity)}
                    </p>
                    <p className="text-xs text-gray-600">
                      {formatCurrency(item.price)} × {item.quantity}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="bg-gray-50 p-4 space-y-2">
            <div className="flex justify-between text-sm text-gray-600">
              <span>Tạm tính:</span>
              <span>{formatCurrency(order.totalAmount)}</span>
            </div>
            {order.shippingFee > 0 && (
              <div className="flex justify-between text-sm text-gray-600">
                <span>Phí vận chuyển:</span>
                <span>{formatCurrency(order.shippingFee)}</span>
              </div>
            )}
            {order.discount > 0 && (
              <div className="flex justify-between text-sm text-green-600">
                <span>Giảm giá:</span>
                <span>-{formatCurrency(order.discount)}</span>
              </div>
            )}
            <div className="pt-2 border-t border-gray-300">
              <div className="flex justify-between text-lg font-bold text-gray-900">
                <span>Tổng cộng:</span>
                <span className="text-blue-600">{formatCurrency(order.totalAmount)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Cancellation Reason (if cancelled) */}
      {order.status === 'CANCELLED' && order.cancellationReason && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <h3 className="font-semibold text-red-900 mb-2">Lý do hủy đơn</h3>
          <p className="text-sm text-red-700">{order.cancellationReason}</p>
        </div>
      )}
    </div>
  );
};

OrderDetails.propTypes = {
  order: PropTypes.shape({
    orderNumber: PropTypes.string.isRequired,
    trackingToken: PropTypes.string,
    createdAt: PropTypes.string.isRequired,
    status: PropTypes.string.isRequired,
    recipientName: PropTypes.string.isRequired,
    phoneNumber: PropTypes.string.isRequired,
    address: PropTypes.string.isRequired,
    email: PropTypes.string,
    notes: PropTypes.string,
    paymentMethod: PropTypes.string.isRequired,
    items: PropTypes.arrayOf(
      PropTypes.shape({
        productName: PropTypes.string.isRequired,
        size: PropTypes.string.isRequired,
        color: PropTypes.string.isRequired,
        quantity: PropTypes.number.isRequired,
        price: PropTypes.number.isRequired,
      })
    ).isRequired,
    totalAmount: PropTypes.number.isRequired,
    shippingFee: PropTypes.number,
    discount: PropTypes.number,
    cancellationReason: PropTypes.string,
  }),
};

export default OrderDetails;
