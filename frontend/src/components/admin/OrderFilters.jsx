import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { FaSearch, FaFilter, FaTimes } from 'react-icons/fa';
import Button from '../common/Button';
import Select from '../common/Select';
import Input from '../common/Input';

/**
 * OrderFilters Component
 * Provides filtering options for the order list
 */
const OrderFilters = ({ onApplyFilters, initialFilters = {} }) => {
  const [filters, setFilters] = useState({
    status: initialFilters.status || '',
    startDate: initialFilters.startDate || '',
    endDate: initialFilters.endDate || '',
    search: initialFilters.search || '',
  });

  const statusOptions = [
    { value: '', label: 'Tất cả trạng thái' },
    { value: 'PENDING_PAYMENT', label: 'Chờ thanh toán' },
    { value: 'CONFIRMED', label: 'Đã xác nhận' },
    { value: 'SHIPPING', label: 'Đang giao hàng' },
    { value: 'DELIVERED', label: 'Đã giao hàng' },
    { value: 'CANCELLED', label: 'Đã hủy' },
  ];

  const handleChange = (name, value) => {
    setFilters((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleApply = () => {
    onApplyFilters(filters);
  };

  const handleClear = () => {
    const clearedFilters = {
      status: '',
      startDate: '',
      endDate: '',
      search: '',
    };
    setFilters(clearedFilters);
    onApplyFilters(clearedFilters);
  };

  const hasActiveFilters = Object.values(filters).some((value) => value !== '');

  return (
    <div className="bg-white rounded-lg shadow p-6 mb-6">
      <div className="flex items-center gap-2 mb-4">
        <FaFilter className="text-gray-500" />
        <h3 className="text-lg font-semibold text-gray-900">Bộ lọc</h3>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Status Filter */}
        <div>
          <Select
            label="Trạng thái"
            name="status"
            value={filters.status}
            onChange={(e) => handleChange('status', e.target.value)}
            options={statusOptions}
          />
        </div>

        {/* Start Date */}
        <div>
          <Input
            label="Từ ngày"
            type="date"
            name="startDate"
            value={filters.startDate}
            onChange={(e) => handleChange('startDate', e.target.value)}
          />
        </div>

        {/* End Date */}
        <div>
          <Input
            label="Đến ngày"
            type="date"
            name="endDate"
            value={filters.endDate}
            onChange={(e) => handleChange('endDate', e.target.value)}
          />
        </div>

        {/* Search */}
        <div>
          <Input
            label="Tìm kiếm"
            type="text"
            name="search"
            placeholder="Mã đơn hàng hoặc tên khách hàng"
            value={filters.search}
            onChange={(e) => handleChange('search', e.target.value)}
            icon={<FaSearch />}
          />
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3 mt-4">
        <Button
          variant="primary"
          onClick={handleApply}
          className="flex items-center gap-2"
        >
          <FaFilter />
          Áp dụng
        </Button>
        {hasActiveFilters && (
          <Button
            variant="outline"
            onClick={handleClear}
            className="flex items-center gap-2"
          >
            <FaTimes />
            Xóa bộ lọc
          </Button>
        )}
      </div>

      {/* Active Filters Summary */}
      {hasActiveFilters && (
        <div className="mt-4 p-3 bg-blue-50 rounded-md">
          <div className="text-sm text-blue-800">
            <strong>Đang lọc:</strong>
            {filters.status && (
              <span className="ml-2">
                Trạng thái: <strong>{statusOptions.find(opt => opt.value === filters.status)?.label}</strong>
              </span>
            )}
            {filters.startDate && (
              <span className="ml-2">
                Từ: <strong>{filters.startDate}</strong>
              </span>
            )}
            {filters.endDate && (
              <span className="ml-2">
                Đến: <strong>{filters.endDate}</strong>
              </span>
            )}
            {filters.search && (
              <span className="ml-2">
                Tìm: <strong>{filters.search}</strong>
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

OrderFilters.propTypes = {
  onApplyFilters: PropTypes.func.isRequired,
  initialFilters: PropTypes.object,
};

export default OrderFilters;
