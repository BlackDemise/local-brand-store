import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import OrderTable from '../../components/admin/OrderTable';
import OrderFilters from '../../components/admin/OrderFilters';
import StatusUpdateModal from '../../components/admin/StatusUpdateModal';
import Pagination from '../../components/common/Pagination';
import { getOrders, updateOrderStatus } from '../../api/orderApi';
import { ADMIN_ITEMS_PER_PAGE } from '../../utils/constants';

/**
 * OrderListPage Component
 * Admin page for managing orders with filters and pagination
 */
const OrderListPage = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // State
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  
  // Modal state
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [updating, setUpdating] = useState(false);

  // Get filters from URL
  const currentPage = parseInt(searchParams.get('page') || '0');
  const status = searchParams.get('status') || '';
  const startDate = searchParams.get('startDate') || '';
  const endDate = searchParams.get('endDate') || '';
  const search = searchParams.get('search') || '';

  // Fetch orders
  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError(null);

      const filters = {
        page: currentPage,
        size: ADMIN_ITEMS_PER_PAGE,
      };

      if (status) filters.status = status;
      if (startDate) filters.startDate = startDate;
      if (endDate) filters.endDate = endDate;
      if (search) filters.search = search;

      const data = await getOrders(filters);
      setOrders(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      console.error('Error fetching orders:', err);
      setError(err.message || 'Không thể tải danh sách đơn hàng');
      toast.error('Không thể tải danh sách đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  // Load orders on mount and when filters change
  useEffect(() => {
    fetchOrders();
  }, [currentPage, status, startDate, endDate, search]);

  // Handle filter apply
  const handleApplyFilters = (filters) => {
    const params = new URLSearchParams();
    params.set('page', '0'); // Reset to first page

    if (filters.status) params.set('status', filters.status);
    if (filters.startDate) params.set('startDate', filters.startDate);
    if (filters.endDate) params.set('endDate', filters.endDate);
    if (filters.search) params.set('search', filters.search);

    setSearchParams(params);
  };

  // Handle page change
  const handlePageChange = (newPage) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', newPage.toString());
    setSearchParams(params);
  };

  // Handle view details
  const handleViewDetails = (orderId) => {
    navigate(`/admin/orders/${orderId}`);
  };

  // Handle update status
  const handleUpdateStatus = (order) => {
    setSelectedOrder(order);
    setShowStatusModal(true);
  };

  // Handle status update submit
  const handleStatusUpdateSubmit = async (orderId, newStatus, reason) => {
    try {
      setUpdating(true);
      await updateOrderStatus(orderId, newStatus);
      
      toast.success('Cập nhật trạng thái thành công');
      
      // Refresh orders
      await fetchOrders();
      
      setShowStatusModal(false);
      setSelectedOrder(null);
    } catch (err) {
      console.error('Error updating status:', err);
      throw err;
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div>
      {/* Page Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Quản lý đơn hàng</h1>
        <p className="mt-2 text-gray-600">
          Tổng số: <span className="font-semibold">{totalElements}</span> đơn hàng
        </p>
      </div>

      {/* Filters */}
      <OrderFilters
        onApplyFilters={handleApplyFilters}
        initialFilters={{ status, startDate, endDate, search }}
      />

      {/* Error Message */}
      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600">{error}</p>
          <button
            onClick={fetchOrders}
            className="mt-2 text-sm text-red-700 underline hover:no-underline"
          >
            Thử lại
          </button>
        </div>
      )}

      {/* Order Table */}
      <OrderTable
        orders={orders}
        loading={loading}
        onViewDetails={handleViewDetails}
        onUpdateStatus={handleUpdateStatus}
      />

      {/* Pagination */}
      {!loading && totalPages > 1 && (
        <div className="mt-6">
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </div>
      )}

      {/* Status Update Modal */}
      <StatusUpdateModal
        isOpen={showStatusModal}
        onClose={() => {
          setShowStatusModal(false);
          setSelectedOrder(null);
        }}
        order={selectedOrder}
        onUpdate={handleStatusUpdateSubmit}
        loading={updating}
      />
    </div>
  );
};

export default OrderListPage;
