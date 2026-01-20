import React from 'react';
import PropTypes from 'prop-types';
import OrderStatus from './OrderStatus';
import OrderTimeline from './OrderTimeline';
import { FaCopy, FaCheckCircle } from 'react-icons/fa';

/**
 * TrackingInfo Component
 * Displays tracking token and current order status with timeline
 */
const TrackingInfo = ({ order }) => {
  const [copied, setCopied] = React.useState(false);

  if (!order) {
    return null;
  }

  const handleCopyToken = () => {
    if (order.trackingToken) {
      navigator.clipboard.writeText(order.trackingToken);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="space-y-6">
      {/* Current Status Card */}
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg shadow-md p-6 border border-blue-200">
        <h3 className="text-sm font-medium text-gray-700 mb-3">
          Trạng thái hiện tại
        </h3>
        <div className="flex items-center justify-center">
          <OrderStatus status={order.status} size="lg" showIcon={true} />
        </div>

        {/* Tracking Token */}
        {order.trackingToken && (
          <div className="mt-6 pt-6 border-t border-blue-200">
            <label className="text-xs font-medium text-gray-600 uppercase tracking-wide">
              Mã tra cứu đơn hàng
            </label>
            <div className="mt-2 flex items-center gap-2">
              <input
                type="text"
                value={order.trackingToken}
                readOnly
                className="flex-1 px-4 py-2 bg-white border border-gray-300 rounded-md font-mono text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                onClick={handleCopyToken}
                className="px-4 py-2 bg-white border border-gray-300 rounded-md hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500"
                title="Sao chép mã tra cứu"
              >
                {copied ? (
                  <FaCheckCircle className="text-green-500" />
                ) : (
                  <FaCopy className="text-gray-500" />
                )}
              </button>
            </div>
            <p className="mt-2 text-xs text-gray-600">
              Lưu mã này để tra cứu đơn hàng sau này
            </p>
          </div>
        )}
      </div>

      {/* Order Timeline */}
      {order.statusHistory && order.statusHistory.length > 0 && (
        <OrderTimeline
          statusHistory={order.statusHistory}
          currentStatus={order.status}
        />
      )}

      {/* Delivery Estimate (Future Enhancement) */}
      {order.status === 'SHIPPING' && order.estimatedDelivery && (
        <div className="bg-purple-50 rounded-lg p-4 border border-purple-200">
          <div className="flex items-start gap-3">
            <div className="flex-shrink-0">
              <svg
                className="w-5 h-5 text-purple-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-purple-900">
                Dự kiến giao hàng
              </p>
              <p className="text-sm text-purple-700 mt-1">
                {order.estimatedDelivery}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Success Message for Delivered Orders */}
      {order.status === 'DELIVERED' && (
        <div className="bg-green-50 rounded-lg p-4 border border-green-200">
          <div className="flex items-start gap-3">
            <div className="flex-shrink-0">
              <FaCheckCircle className="w-5 h-5 text-green-600" />
            </div>
            <div>
              <p className="text-sm font-medium text-green-900">
                Đơn hàng đã được giao thành công!
              </p>
              <p className="text-sm text-green-700 mt-1">
                Cảm ơn bạn đã mua hàng. Hy vọng bạn hài lòng với sản phẩm!
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Cancellation Notice */}
      {order.status === 'CANCELLED' && (
        <div className="bg-red-50 rounded-lg p-4 border border-red-200">
          <div className="flex items-start gap-3">
            <div className="flex-shrink-0">
              <svg
                className="w-5 h-5 text-red-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-red-900">
                Đơn hàng đã bị hủy
              </p>
              {order.cancellationReason && (
                <p className="text-sm text-red-700 mt-1">
                  Lý do: {order.cancellationReason}
                </p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

TrackingInfo.propTypes = {
  order: PropTypes.shape({
    trackingToken: PropTypes.string,
    status: PropTypes.string.isRequired,
    statusHistory: PropTypes.array,
    estimatedDelivery: PropTypes.string,
    cancellationReason: PropTypes.string,
  }),
};

export default TrackingInfo;
