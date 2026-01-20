import React, { useState } from 'react';
import Input from '../common/Input';
import { validatePhone } from '../../utils/validators';

/**
 * Shipping information form
 */
const ShippingForm = ({ formData, onChange, errors }) => {
  const handleChange = (e) => {
    const { name, value } = e.target;
    onChange({ ...formData, [name]: value });
  };
  
  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Thông tin giao hàng
      </h3>
      
      {/* Recipient Name */}
      <Input
        label="Tên người nhận"
        name="recipientName"
        type="text"
        value={formData.recipientName || ''}
        onChange={handleChange}
        error={errors.recipientName}
        placeholder="Nguyễn Văn A"
        required
      />
      
      {/* Phone Number */}
      <Input
        label="Số điện thoại"
        name="phoneNumber"
        type="tel"
        value={formData.phoneNumber || ''}
        onChange={handleChange}
        error={errors.phoneNumber}
        placeholder="0912345678"
        required
      />
      
      {/* Address */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Địa chỉ giao hàng <span className="text-red-500">*</span>
        </label>
        <textarea
          name="address"
          value={formData.address || ''}
          onChange={handleChange}
          rows={3}
          placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố"
          className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
            errors.address ? 'border-red-500' : 'border-gray-300'
          }`}
          required
        />
        {errors.address && (
          <p className="mt-1 text-sm text-red-500">{errors.address}</p>
        )}
      </div>
      
      {/* Notes */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Ghi chú (tùy chọn)
        </label>
        <textarea
          name="notes"
          value={formData.notes || ''}
          onChange={handleChange}
          rows={2}
          placeholder="Ghi chú thêm cho đơn hàng (nếu có)"
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>
    </div>
  );
};

export default ShippingForm;
