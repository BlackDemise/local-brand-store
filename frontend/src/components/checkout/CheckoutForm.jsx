import React from 'react';
import ShippingForm from './ShippingForm';
import PaymentSelector from './PaymentSelector';
import Button from '../common/Button';

/**
 * Main checkout form container
 */
const CheckoutForm = ({ 
  shippingData, 
  setShippingData, 
  paymentMethod, 
  setPaymentMethod,
  errors,
  onSubmit,
  loading = false,
  disabled = false
}) => {
  
  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit();
  };
  
  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Shipping Form */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <ShippingForm
          formData={shippingData}
          onChange={setShippingData}
          errors={errors}
        />
      </div>
      
      {/* Payment Selector */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <PaymentSelector
          selectedMethod={paymentMethod}
          onChange={setPaymentMethod}
        />
      </div>
      
      {/* Submit Button */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <Button
          type="submit"
          variant="primary"
          className="w-full"
          disabled={disabled || loading}
          loading={loading}
        >
          {loading ? 'Đang xử lý...' : 'Đặt hàng'}
        </Button>
        
        <p className="mt-3 text-xs text-gray-500 text-center">
          Bằng cách đặt hàng, bạn đồng ý với{' '}
          <a href="#" className="text-blue-600 hover:underline">
            Điều khoản dịch vụ
          </a>{' '}
          và{' '}
          <a href="#" className="text-blue-600 hover:underline">
            Chính sách bảo mật
          </a>
        </p>
      </div>
    </form>
  );
};

export default CheckoutForm;
