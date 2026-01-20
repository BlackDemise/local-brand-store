import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import {
  FaArrowLeft,
  FaEdit,
  FaTimes,
  FaMapMarkerAlt,
  FaPhone,
  FaUser,
  FaCreditCard,
} from 'react-icons/fa';
import Button from '../../components/common/Button';
import Spinner from '../../components/common/Spinner';
import StatusBadge from '../../components/admin/StatusBadge';
import StatusUpdateModal from '../../components/admin/StatusUpdateModal';
import OrderHistoryView from '../../components/admin/OrderHistoryView';
import Modal from '../../components/common/Modal';
import { getOrderById, getOrderHistory, updateOrderStatus, cancelOrder } from '../../api/orderApi';
import { formatCurrency, formatDate, formatDateTime } from '../../utils/formatters';
import { PAYMENT_METHOD_LABELS } from '../../utils/constants';

/**
 * OrderDetailPage Component
 * Admin page for viewing and managing individual order details
 */
const OrderDetailPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();

  // State
  const [order, setOrder] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [error, setError] = useState(null);

  // Modal state
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [updating, setUpdating] = useState(false);
  const [cancelReason, setCancelReason] = useState('');

  // Fetch order details
  const fetchOrder = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getOrderById(orderId);
      setOrder(data);
    } catch (err) {
      console.error('Error fetching order:', err);
      setError(err.message || 'Không thể tải thông tin đơn hàng');
      toast.error('Không thể tải thông tin đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  // Fetch order history
  const fetchHistory = async () => {
    try {
      setHistoryLoading(true);
      const data = await getOrderHistory(orderId);
      setHistory(data || []);
    } catch (err) {
      console.error('Error fetching history:', err);
      toast.error('Không thể tải lịch sử đơn hàng');
    } finally {
      setHistoryLoading(false);
    }
  };

  // Load data on mount
  useEffect(() => {
    fetchOrder();
    fetchHistory();
  }, [orderId]);

  // Handle status update
  const handleStatusUpdate = async (orderId, newStatus, reason) => {
    try {
      setUpdating(true);
      await updateOrderStatus(orderId, newStatus);
      
      toast.success('Cập nhật trạng thái thành công');
      
      // Refresh data
      await fetchOrder();
      await fetchHistory();
      
      setShowStatusModal(false);
    } catch (err) {
      console.error('Error updating status:', err);
      throw err;
    } finally {
      setUpdating(false);
    }
  };

  // Handle cancel order
  const handleCancelOrder = async () => {
    if (!cancelReason.trim()) {
      toast.error('Vui lòng nhập lý do hủy');
      return;
    }

    try {
      setUpdating(true);
      await cancelOrder(orderId, cancelReason);
      
      toast.success('Đã hủy đơn hàng');
      
      // Refresh data
      await fetchOrder();
      await fetchHistory();
      
      setShowCancelModal(false);
      setCancelReason('');
    } catch (err) {
      console.error('Error cancelling order:', err);
      toast.error(err.message || 'Không thể hủy đơn hàng');
    } finally {
      setUpdating(false);
    }
  };

  // Loading state
  if (loading) {
    return (
      <div className="flex justify-center items-center h-96">
        <Spinner size="lg" />
      </div>
    );
  }

  // Error state
  if (error || !order) {
    return (
      <div className="text-center py-12">
        <div className="text-red-500 text-6xl mb-4">⚠️</div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">
          Không tìm thấy đơn hàng
        </h2>
        <p className="text-gray-600 mb-6">{error}</p>
        <Button onClick={() => navigate('/admin/orders')}>
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  const canUpdateStatus = order.status !== 'DELIVERED' && order.status !== 'CANCELLED';
  const canCancel = order.status !== 'DELIVERED' && order.status !== 'CANCELLED';

  return (
    <div>
      {/* Header */}
      <div className="mb-6">
        <Button
          variant="outline"
          onClick={() => navigate('/admin/orders')}
          className="mb-4"
        >
          <FaArrowLeft className="mr-2" />
          Quay lại danh sách
        </Button>

        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              Đơn hàng #{order.orderNumber}
            </h1>
            <p className="mt-1 text-gray-600">
              Đặt ngày {formatDateTime(order.orderDate)}
            </p>
          </div>
          <StatusBadge status={order.status} size="lg" />
        </div>
      </div>

      {/* Actions */}
      {canUpdateStatus && (
        <div className="mb-6 flex gap-3">
          <Button
            variant="primary"
            onClick={() => setShowStatusModal(true)}
          >
            <FaEdit className="mr-2" />
            Cập nhật trạng thái
          </Button>
          {canCancel && (
            <Button
              variant="danger"
              onClick={() => setShowCancelModal(true)}
            >
              <FaTimes className="mr-2" />
              Hủy đơn hàng
            </Button>
          )}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left Column */}
        <div className="space-y-6">
          {/* Order Information */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Thông tin đơn hàng</h2>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Mã theo dõi:</span>
                <span className="font-mono text-sm">{order.trackingToken}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Ngày đặt:</span>
                <span className="font-medium">{formatDateTime(order.orderDate)}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600 flex items-center gap-2">
                  <FaCreditCard /> Thanh toán:
                </span>
                <span className="font-medium">
                  {PAYMENT_METHOD_LABELS[order.paymentMethod] || order.paymentMethod}
                </span>
              </div>
            </div>
          </div>

          {/* Customer Information */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Thông tin khách hàng</h2>
            <div className="space-y-3">
              <div className="flex items-start gap-3">
                <FaUser className="text-gray-400 mt-1" />
                <div>
                  <div className="text-sm text-gray-600">Người nhận:</div>
                  <div className="font-medium">{order.recipientName}</div>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <FaPhone className="text-gray-400 mt-1" />
                <div>
                  <div className="text-sm text-gray-600">Số điện thoại:</div>
                  <div className="font-medium">{order.phone}</div>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <FaMapMarkerAlt className="text-gray-400 mt-1" />
                <div>
                  <div className="text-sm text-gray-600">Địa chỉ giao hàng:</div>
                  <div className="font-medium">{order.address}</div>
                </div>
              </div>
              {order.notes && (
                <div className="pt-3 border-t">
                  <div className="text-sm text-gray-600 mb-1">Ghi chú:</div>
                  <div className="text-gray-700 italic">{order.notes}</div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Column */}
        <div className="space-y-6">
          {/* Order Items */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Sản phẩm đã đặt</h2>
            <div className="space-y-4">
              {order.items && order.items.map((item, index) => (
                <div
                  key={index}
                  className="flex gap-4 pb-4 border-b last:border-b-0 last:pb-0"
                >
                  <div className="flex-1">
                    <h3 className="font-medium text-gray-900">
                      {item.productName}
                    </h3>
                    <p className="text-sm text-gray-600">
                      Size: {item.size} | Màu: {item.color}
                    </p>
                    <p className="text-sm text-gray-600">
                      Số lượng: {item.quantity}
                    </p>
                  </div>
                  <div className="text-right">
                    <div className="font-semibold text-gray-900">
                      {formatCurrency(item.subtotal)}
                    </div>
                    <div className="text-sm text-gray-600">
                      {formatCurrency(item.unitPrice)} × {item.quantity}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Total */}
            <div className="mt-6 pt-4 border-t">
              <div className="flex justify-between text-lg font-bold">
                <span>Tổng cộng:</span>
                <span className="text-blue-600">
                  {formatCurrency(order.totalAmount)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Order History */}
      <div className="mt-6">
        <h2 className="text-xl font-semibold mb-4">Lịch sử thay đổi</h2>
        <OrderHistoryView history={history} loading={historyLoading} />
      </div>

      {/* Status Update Modal */}
      <StatusUpdateModal
        isOpen={showStatusModal}
        onClose={() => setShowStatusModal(false)}
        order={order}
        onUpdate={handleStatusUpdate}
        loading={updating}
      />

      {/* Cancel Order Modal */}
      <Modal
        isOpen={showCancelModal}
        onClose={() => {
          setShowCancelModal(false);
          setCancelReason('');
        }}
        title="Xác nhận hủy đơn hàng"
        footer={
          <div className="flex justify-end gap-3">
            <Button
              variant="outline"
              onClick={() => {
                setShowCancelModal(false);
                setCancelReason('');
              }}
              disabled={updating}
            >
              Quay lại
            </Button>
            <Button
              variant="danger"
              onClick={handleCancelOrder}
              loading={updating}
              disabled={!cancelReason.trim() || updating}
            >
              Xác nhận hủy
            </Button>
          </div>
        }
      >
        <div className="space-y-4">
          <p className="text-gray-600">
            Bạn có chắc chắn muốn hủy đơn hàng <strong>#{order.orderNumber}</strong>?
          </p>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Lý do hủy <span className="text-red-500">*</span>
            </label>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Nhập lý do hủy đơn hàng..."
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default OrderDetailPage;
