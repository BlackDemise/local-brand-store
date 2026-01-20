import React from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import OrderStatus from './OrderStatus';
import Button from '../common/Button';
import { formatCurrency, formatDateTime } from '../../utils/formatters';

/**
 * OrderCard Component
 * Compact card display for order listing (order history, admin order list)
 */
const OrderCard = ({ order, showActions = true, onViewDetails }) => {
  const navigate = useNavigate();

  const handleViewDetails = () => {
    if (onViewDetails) {
      onViewDetails(order);
    } else {
      // Default: navigate to tracking page
      navigate(`/track/${order.trackingToken}`);
    }
  };

  const getItemsSummary = (items) => {
    if (!items || items.length === 0) return 'Không có sản phẩm';
    
    const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
    return `${totalItems} sản phẩm`;
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition-shadow">
      {/* Header */}
      <div className="flex justify-between items-start mb-3">
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900 text-lg">
            Đơn hàng #{order.orderNumber}
          </h3>
          <p className="text-sm text-gray-600 mt-1">
            {formatDateTime(order.createdAt)}
          </p>
        </div>
        <OrderStatus status={order.status} size="sm" showIcon={false} />
      </div>

      {/* Order Summary */}
      <div className="border-t border-gray-200 pt-3 mb-3 space-y-2">
        <div className="flex justify-between text-sm">
          <span className="text-gray-600">Khách hàng:</span>
          <span className="font-medium text-gray-900">{order.recipientName}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-600">Sản phẩm:</span>
          <span className="font-medium text-gray-900">{getItemsSummary(order.items)}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-600">Tổng tiền:</span>
          <span className="font-bold text-blue-600">{formatCurrency(order.totalAmount)}</span>
        </div>
      </div>

      {/* Actions */}
      {showActions && (
        <div className="border-t border-gray-200 pt-3">
          <Button
            onClick={handleViewDetails}
            variant="outline"
            size="sm"
            className="w-full"
          >
            Xem chi tiết
          </Button>
        </div>
      )}
    </div>
  );
};

OrderCard.propTypes = {
  order: PropTypes.shape({
    orderNumber: PropTypes.string.isRequired,
    trackingToken: PropTypes.string,
    createdAt: PropTypes.string.isRequired,
    status: PropTypes.string.isRequired,
    recipientName: PropTypes.string.isRequired,
    items: PropTypes.array,
    totalAmount: PropTypes.number.isRequired,
  }).isRequired,
  showActions: PropTypes.bool,
  onViewDetails: PropTypes.func,
};

export default OrderCard;
