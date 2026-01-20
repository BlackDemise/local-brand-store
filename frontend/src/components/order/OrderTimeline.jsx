import React from 'react';
import PropTypes from 'prop-types';
import { formatDateTime } from '../../utils/formatters';
import {
  FaClock,
  FaCheckCircle,
  FaTruck,
  FaBoxOpen,
  FaTimesCircle,
} from 'react-icons/fa';

/**
 * OrderTimeline Component
 * Vertical timeline showing order status progression with timestamps
 */
const OrderTimeline = ({ statusHistory, currentStatus }) => {
  const getStatusConfig = (status) => {
    switch (status) {
      case 'PENDING_PAYMENT':
        return {
          icon: FaClock,
          label: 'Chờ thanh toán',
          color: 'yellow',
        };
      case 'CONFIRMED':
        return {
          icon: FaCheckCircle,
          label: 'Đã xác nhận',
          color: 'blue',
        };
      case 'SHIPPING':
        return {
          icon: FaTruck,
          label: 'Đang giao hàng',
          color: 'purple',
        };
      case 'DELIVERED':
        return {
          icon: FaBoxOpen,
          label: 'Đã giao hàng',
          color: 'green',
        };
      case 'CANCELLED':
        return {
          icon: FaTimesCircle,
          label: 'Đã hủy',
          color: 'red',
        };
      default:
        return {
          icon: FaClock,
          label: status,
          color: 'gray',
        };
    }
  };

  const getColorClasses = (color, isActive) => {
    const colors = {
      yellow: {
        bg: isActive ? 'bg-yellow-500' : 'bg-yellow-200',
        text: isActive ? 'text-yellow-700' : 'text-yellow-600',
        border: 'border-yellow-300',
      },
      blue: {
        bg: isActive ? 'bg-blue-500' : 'bg-blue-200',
        text: isActive ? 'text-blue-700' : 'text-blue-600',
        border: 'border-blue-300',
      },
      purple: {
        bg: isActive ? 'bg-purple-500' : 'bg-purple-200',
        text: isActive ? 'text-purple-700' : 'text-purple-600',
        border: 'border-purple-300',
      },
      green: {
        bg: isActive ? 'bg-green-500' : 'bg-green-200',
        text: isActive ? 'text-green-700' : 'text-green-600',
        border: 'border-green-300',
      },
      red: {
        bg: isActive ? 'bg-red-500' : 'bg-red-200',
        text: isActive ? 'text-red-700' : 'text-red-600',
        border: 'border-red-300',
      },
      gray: {
        bg: 'bg-gray-200',
        text: 'text-gray-500',
        border: 'border-gray-300',
      },
    };
    return colors[color] || colors.gray;
  };

  if (!statusHistory || statusHistory.length === 0) {
    return (
      <div className="bg-gray-50 rounded-lg p-6 text-center text-gray-500">
        Chưa có lịch sử trạng thái
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="font-semibold text-gray-900 mb-6 text-lg">
        Lịch sử đơn hàng
      </h3>

      <div className="relative">
        {statusHistory.map((historyItem, index) => {
          const config = getStatusConfig(historyItem.status);
          const Icon = config.icon;
          const isActive = historyItem.status === currentStatus;
          const isLast = index === statusHistory.length - 1;
          const colors = getColorClasses(config.color, isActive);

          return (
            <div key={index} className="relative pb-8 last:pb-0">
              {/* Vertical Line */}
              {!isLast && (
                <div
                  className={`absolute left-5 top-11 w-0.5 h-full -ml-px ${
                    isActive ? 'bg-gray-400' : 'bg-gray-300'
                  }`}
                />
              )}

              {/* Timeline Item */}
              <div className="relative flex items-start gap-4">
                {/* Icon Circle */}
                <div
                  className={`flex-shrink-0 w-10 h-10 rounded-full ${colors.bg} flex items-center justify-center shadow-md z-10 ${
                    isActive ? 'ring-4 ring-offset-2 ring-opacity-50' : ''
                  } ${colors.border.replace('border-', 'ring-')}`}
                >
                  <Icon
                    className={`text-white ${
                      isActive ? 'text-base' : 'text-sm'
                    }`}
                  />
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0 pt-1">
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                    <div>
                      <p
                        className={`font-semibold ${
                          isActive ? 'text-gray-900' : 'text-gray-700'
                        }`}
                      >
                        {config.label}
                      </p>
                      <p className="text-sm text-gray-500 mt-1">
                        {formatDateTime(historyItem.createdAt)}
                      </p>
                    </div>
                    {isActive && (
                      <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        Trạng thái hiện tại
                      </span>
                    )}
                  </div>

                  {/* Additional Info */}
                  {historyItem.changedBy && (
                    <p className="text-xs text-gray-600 mt-2">
                      Cập nhật bởi: {historyItem.changedBy}
                    </p>
                  )}
                  {historyItem.notes && (
                    <p className="text-sm text-gray-600 mt-2 bg-gray-50 rounded-md p-3 border border-gray-200">
                      {historyItem.notes}
                    </p>
                  )}
                  {historyItem.reason && (
                    <p className="text-sm text-gray-600 mt-2 bg-red-50 rounded-md p-3 border border-red-200">
                      <span className="font-medium text-red-700">Lý do: </span>
                      {historyItem.reason}
                    </p>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

OrderTimeline.propTypes = {
  statusHistory: PropTypes.arrayOf(
    PropTypes.shape({
      status: PropTypes.string.isRequired,
      createdAt: PropTypes.string.isRequired,
      changedBy: PropTypes.string,
      notes: PropTypes.string,
      reason: PropTypes.string,
    })
  ).isRequired,
  currentStatus: PropTypes.string.isRequired,
};

export default OrderTimeline;
