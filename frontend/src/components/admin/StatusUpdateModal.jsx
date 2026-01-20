import React, { useState } from 'react';
import PropTypes from 'prop-types';
import Modal from '../common/Modal';
import Select from '../common/Select';
import Button from '../common/Button';
import StatusBadge from './StatusBadge';
import { FaCheckCircle } from 'react-icons/fa';

/**
 * StatusUpdateModal Component
 * Modal for updating order status with validation
 */
const StatusUpdateModal = ({
  isOpen,
  onClose,
  order,
  onUpdate,
  loading = false,
}) => {
  const [newStatus, setNewStatus] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');

  // Valid status transitions based on current status
  const getValidStatuses = (currentStatus) => {
    const transitions = {
      PENDING_PAYMENT: [
        { value: 'CONFIRMED', label: 'Xác nhận đơn hàng' },
        { value: 'CANCELLED', label: 'Hủy đơn hàng' },
      ],
      CONFIRMED: [
        { value: 'SHIPPING', label: 'Bắt đầu giao hàng' },
        { value: 'CANCELLED', label: 'Hủy đơn hàng' },
      ],
      SHIPPING: [
        { value: 'DELIVERED', label: 'Đã giao hàng' },
      ],
      DELIVERED: [],
      CANCELLED: [],
    };

    return transitions[currentStatus] || [];
  };

  const validStatuses = order ? getValidStatuses(order.status) : [];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!newStatus) {
      setError('Vui lòng chọn trạng thái mới');
      return;
    }

    if (newStatus === 'CANCELLED' && !reason.trim()) {
      setError('Vui lòng nhập lý do hủy đơn hàng');
      return;
    }

    try {
      await onUpdate(order.id, newStatus, reason);
      handleClose();
    } catch (err) {
      setError(err.message || 'Có lỗi xảy ra khi cập nhật trạng thái');
    }
  };

  const handleClose = () => {
    setNewStatus('');
    setReason('');
    setError('');
    onClose();
  };

  if (!order) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title="Cập nhật trạng thái đơn hàng"
      footer={
        <div className="flex justify-end gap-3">
          <Button variant="outline" onClick={handleClose} disabled={loading}>
            Hủy
          </Button>
          <Button
            variant="primary"
            onClick={handleSubmit}
            loading={loading}
            disabled={!newStatus || loading}
          >
            <FaCheckCircle className="mr-2" />
            Cập nhật
          </Button>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Order Info */}
        <div className="bg-gray-50 p-4 rounded-lg">
          <div className="flex items-center justify-between mb-2">
            <span className="text-sm font-medium text-gray-600">Đơn hàng:</span>
            <span className="text-sm font-semibold text-gray-900">
              #{order.orderNumber}
            </span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-gray-600">Trạng thái hiện tại:</span>
            <StatusBadge status={order.status} size="sm" />
          </div>
        </div>

        {/* Status Selector */}
        {validStatuses.length > 0 ? (
          <>
            <div>
              <Select
                label="Trạng thái mới"
                name="newStatus"
                value={newStatus}
                onChange={(e) => setNewStatus(e.target.value)}
                options={[
                  { value: '', label: 'Chọn trạng thái mới' },
                  ...validStatuses,
                ]}
                required
              />
            </div>

            {/* Reason field (required for cancellation) */}
            {newStatus === 'CANCELLED' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Lý do hủy <span className="text-red-500">*</span>
                </label>
                <textarea
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="Nhập lý do hủy đơn hàng..."
                  rows={4}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
            )}

            {/* Error Message */}
            {error && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-md">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}
          </>
        ) : (
          <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
            <p className="text-sm text-yellow-800">
              {order.status === 'DELIVERED'
                ? 'Đơn hàng đã được giao, không thể thay đổi trạng thái.'
                : 'Đơn hàng đã bị hủy, không thể thay đổi trạng thái.'}
            </p>
          </div>
        )}
      </form>
    </Modal>
  );
};

StatusUpdateModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  order: PropTypes.object,
  onUpdate: PropTypes.func.isRequired,
  loading: PropTypes.bool,
};

export default StatusUpdateModal;
