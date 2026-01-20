import React from 'react';

/**
 * Payment method selector
 */
const PaymentSelector = ({ selectedMethod, onChange }) => {
  const paymentMethods = [
    {
      id: 'COD',
      name: 'Thanh toán khi nhận hàng (COD)',
      description: 'Thanh toán bằng tiền mặt khi nhận hàng',
      icon: '💵',
    },
    {
      id: 'BANK_TRANSFER',
      name: 'Chuyển khoản ngân hàng',
      description: 'Chuyển khoản trước khi nhận hàng',
      icon: '🏦',
    },
  ];
  
  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Phương thức thanh toán
      </h3>
      
      <div className="space-y-3">
        {paymentMethods.map((method) => (
          <label
            key={method.id}
            className={`block border-2 rounded-lg p-4 cursor-pointer transition-colors ${
              selectedMethod === method.id
                ? 'border-blue-600 bg-blue-50'
                : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <div className="flex items-start">
              <input
                type="radio"
                name="paymentMethod"
                value={method.id}
                checked={selectedMethod === method.id}
                onChange={(e) => onChange(e.target.value)}
                className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500"
              />
              <div className="ml-3 flex-1">
                <div className="flex items-center gap-2">
                  <span className="text-2xl">{method.icon}</span>
                  <span className="font-medium text-gray-900">{method.name}</span>
                </div>
                <p className="mt-1 text-sm text-gray-600">{method.description}</p>
              </div>
            </div>
          </label>
        ))}
      </div>
      
      {/* Bank Transfer Instructions */}
      {selectedMethod === 'BANK_TRANSFER' && (
        <div className="mt-4 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 className="font-semibold text-blue-900 mb-2">
            Thông tin chuyển khoản:
          </h4>
          <div className="text-sm text-blue-800 space-y-1">
            <p><strong>Ngân hàng:</strong> Vietcombank</p>
            <p><strong>Số tài khoản:</strong> 1234567890</p>
            <p><strong>Chủ tài khoản:</strong> CONG TY LOCAL BRAND STORE</p>
            <p className="mt-2">
              <strong>Nội dung:</strong> Mã đơn hàng (sẽ được cung cấp sau khi đặt hàng)
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default PaymentSelector;
