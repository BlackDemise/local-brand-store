import React from 'react';
import PropTypes from 'prop-types';
import {
  FaClock,
  FaCheckCircle,
  FaTruck,
  FaBoxOpen,
  FaTimesCircle,
} from 'react-icons/fa';

/**
 * OrderStatus Component
 * Color-coded status badge with icon and text
 */
const OrderStatus = ({ status, size = 'md', showIcon = true, className = '' }) => {
  const getStatusConfig = (status) => {
    switch (status) {
      case 'PENDING_PAYMENT':
        return {
          color: 'bg-yellow-100 text-yellow-800 border-yellow-300',
          text: 'Chờ thanh toán',
          icon: FaClock,
        };
      case 'CONFIRMED':
        return {
          color: 'bg-blue-100 text-blue-800 border-blue-300',
          text: 'Đã xác nhận',
          icon: FaCheckCircle,
        };
      case 'SHIPPING':
        return {
          color: 'bg-purple-100 text-purple-800 border-purple-300',
          text: 'Đang giao hàng',
          icon: FaTruck,
        };
      case 'DELIVERED':
        return {
          color: 'bg-green-100 text-green-800 border-green-300',
          text: 'Đã giao hàng',
          icon: FaBoxOpen,
        };
      case 'CANCELLED':
        return {
          color: 'bg-red-100 text-red-800 border-red-300',
          text: 'Đã hủy',
          icon: FaTimesCircle,
        };
      default:
        return {
          color: 'bg-gray-100 text-gray-800 border-gray-300',
          text: status,
          icon: FaClock,
        };
    }
  };

  const getSizeClasses = (size) => {
    switch (size) {
      case 'sm':
        return 'px-2 py-1 text-xs';
      case 'lg':
        return 'px-6 py-3 text-lg';
      case 'md':
      default:
        return 'px-4 py-2 text-sm';
    }
  };

  const config = getStatusConfig(status);
  const Icon = config.icon;
  const sizeClasses = getSizeClasses(size);

  return (
    <span
      className={`inline-flex items-center gap-2 rounded-full font-medium border ${config.color} ${sizeClasses} ${className}`}
    >
      {showIcon && <Icon className="flex-shrink-0" />}
      <span>{config.text}</span>
    </span>
  );
};

OrderStatus.propTypes = {
  status: PropTypes.oneOf([
    'PENDING_PAYMENT',
    'CONFIRMED',
    'SHIPPING',
    'DELIVERED',
    'CANCELLED',
  ]).isRequired,
  size: PropTypes.oneOf(['sm', 'md', 'lg']),
  showIcon: PropTypes.bool,
  className: PropTypes.string,
};

export default OrderStatus;
