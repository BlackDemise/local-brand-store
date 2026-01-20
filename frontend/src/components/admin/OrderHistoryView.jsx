import React from 'react';
import PropTypes from 'prop-types';
import { formatDateTime } from '../../utils/formatters';
import StatusBadge from './StatusBadge';
import Spinner from '../common/Spinner';
import { FaHistory } from 'react-icons/fa';

/**
 * OrderHistoryView Component
 * Displays order status change history in a table
 */
const OrderHistoryView = ({ history = [], loading = false }) => {
  if (loading) {
    return (
      <div className="flex justify-center items-center py-8">
        <Spinner size="md" />
      </div>
    );
  }

  if (!history || history.length === 0) {
    return (
      <div className="text-center py-8">
        <FaHistory className="mx-auto text-4xl text-gray-300 mb-3" />
        <p className="text-gray-500">Chưa có lịch sử thay đổi trạng thái</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Thời gian
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Trạng thái cũ
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Trạng thái mới
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Người thay đổi
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ghi chú
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {history.map((entry, index) => (
              <tr key={index} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {formatDateTime(entry.changedAt)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {entry.oldStatus ? (
                    <StatusBadge status={entry.oldStatus} size="sm" showIcon={false} />
                  ) : (
                    <span className="text-sm text-gray-400">-</span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <StatusBadge status={entry.newStatus} size="sm" showIcon={false} />
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {entry.changedBy || 'Hệ thống'}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {entry.notes || '-'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

OrderHistoryView.propTypes = {
  history: PropTypes.arrayOf(
    PropTypes.shape({
      changedAt: PropTypes.string.isRequired,
      oldStatus: PropTypes.string,
      newStatus: PropTypes.string.isRequired,
      changedBy: PropTypes.string,
      notes: PropTypes.string,
    })
  ),
  loading: PropTypes.bool,
};

export default OrderHistoryView;
