import React from 'react';
import PropTypes from 'prop-types';
import { FaEye, FaEdit } from 'react-icons/fa';
import { formatCurrency, formatDate } from '../../utils/formatters';
import OrderStatus from '../order/OrderStatus';
import Button from '../common/Button';
import Spinner from '../common/Spinner';

/**
 * OrderTable Component
 * Displays orders in a table format with actions
 */
const OrderTable = ({
  orders = [],
  loading = false,
  onViewDetails,
  onUpdateStatus,
}) => {
  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!orders || orders.length === 0) {
    return (
      <div className="text-center py-12">
        <div className="text-gray-400 text-6xl mb-4">📦</div>
        <h3 className="text-xl font-semibold text-gray-700 mb-2">
          Không tìm thấy đơn hàng
        </h3>
        <p className="text-gray-500">
          Chưa có đơn hàng nào phù hợp với bộ lọc của bạn
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto bg-white rounded-lg shadow">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Mã đơn hàng
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Ngày đặt
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Khách hàng
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Trạng thái
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Tổng tiền
            </th>
            <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
              Thao tác
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {orders.map((order) => (
            <tr
              key={order.id}
              className="hover:bg-gray-50 transition-colors"
            >
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="text-sm font-medium text-gray-900">
                  #{order.orderNumber}
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="text-sm text-gray-900">
                  {formatDate(order.orderDate)}
                </div>
              </td>
              <td className="px-6 py-4">
                <div className="text-sm text-gray-900">
                  {order.recipientName}
                </div>
                <div className="text-sm text-gray-500">
                  {order.phone}
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <OrderStatus status={order.status} size="sm" />
              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <div className="text-sm font-semibold text-gray-900">
                  {formatCurrency(order.totalAmount)}
                </div>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div className="flex justify-end gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onViewDetails(order.id)}
                    aria-label="Xem chi tiết"
                  >
                    <FaEye className="mr-1" />
                    Chi tiết
                  </Button>
                  {order.status !== 'DELIVERED' && order.status !== 'CANCELLED' && (
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => onUpdateStatus(order)}
                      aria-label="Cập nhật trạng thái"
                    >
                      <FaEdit className="mr-1" />
                      Cập nhật
                    </Button>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

OrderTable.propTypes = {
  orders: PropTypes.array,
  loading: PropTypes.bool,
  onViewDetails: PropTypes.func.isRequired,
  onUpdateStatus: PropTypes.func.isRequired,
};

export default OrderTable;
