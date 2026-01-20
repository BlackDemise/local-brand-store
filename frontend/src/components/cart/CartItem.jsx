import React, { useState } from 'react';
import { formatCurrency } from '../../utils/formatters';
import Button from '../common/Button';

/**
 * Individual cart item row
 * Displays product info with quantity controls and remove button
 */
const CartItem = ({ item, onUpdateQuantity, onRemove, disabled = false }) => {
  console.log(item);
  const [quantity, setQuantity] = useState(item.quantity);
  const [updating, setUpdating] = useState(false);
  
  const handleQuantityChange = async (newQuantity) => {
    if (newQuantity < 1 || newQuantity > item.availableStock) return;
    
    setQuantity(newQuantity);
    setUpdating(true);
    
    try {
      await onUpdateQuantity(item.id, newQuantity);
    } catch (error) {
      // Revert on error
      setQuantity(item.quantity);
    } finally {
      setUpdating(false);
    }
  };
  
  const handleRemove = async () => {
    setUpdating(true);
    try {
      await onRemove(item.id);
    } finally {
      setUpdating(false);
    }
  };
  
  const itemTotal = item.price * item.quantity;
  const hasStockWarning = item.quantity > item.availableStock;
  
  return (
    <div className={`bg-white rounded-lg shadow-sm p-4 ${hasStockWarning ? 'border-2 border-yellow-400' : ''}`}>
      <div className="flex gap-4">
        {/* Product Image */}
        <div className="flex-shrink-0">
          <img
            src={item.imageUrl || '/placeholder-product.png'}
            alt={item.productName}
            className="w-24 h-24 object-cover rounded-md"
          />
        </div>
        
        {/* Product Info */}
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900 mb-1">
            {item.productName}
          </h3>
          
          <div className="text-sm text-gray-600 mb-2">
            {item.size && <span className="mr-3">Kích thước: {item.size}</span>}
            {item.color && <span>Màu: {item.color}</span>}
          </div>
          
          <div className="flex items-center gap-4">
            {/* Quantity Controls */}
            <div className="flex items-center border border-gray-300 rounded-md">
              <button
                onClick={() => handleQuantityChange(quantity - 1)}
                disabled={disabled || updating || quantity <= 1}
                className="px-3 py-1 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                -
              </button>
              <input
                type="number"
                min="1"
                max={item.availableStock}
                value={quantity}
                onChange={(e) => {
                  const val = parseInt(e.target.value);
                  if (!isNaN(val) && val >= 1 && val <= item.availableStock) {
                    handleQuantityChange(val);
                  }
                }}
                disabled={disabled || updating}
                className="w-16 text-center border-x border-gray-300 py-1 focus:outline-none"
              />
              <button
                onClick={() => handleQuantityChange(quantity + 1)}
                disabled={disabled || updating || quantity >= item.availableStock}
                className="px-3 py-1 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                +
              </button>
            </div>
            
            {/* Stock Info */}
            <span className="text-sm text-gray-500">
              {item.availableStock > 0 ? (
                `Còn ${item.availableStock} sản phẩm`
              ) : (
                <span className="text-red-500">Hết hàng</span>
              )}
            </span>
          </div>
          
          {/* Stock Warning */}
          {hasStockWarning && (
            <div className="mt-2 text-sm text-yellow-600 bg-yellow-50 p-2 rounded">
              ⚠️ Số lượng sản phẩm trong kho không đủ. Chỉ còn {item.availableStock} sản phẩm.
            </div>
          )}
        </div>
        
        {/* Price and Remove */}
        <div className="flex flex-col items-end justify-between">
          <button
            onClick={handleRemove}
            disabled={disabled || updating}
            className="text-red-500 hover:text-red-700 disabled:opacity-50"
            title="Xóa sản phẩm"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          
          <div className="text-right">
            <p className="text-sm text-gray-500">{formatCurrency(item.price)}</p>
            <p className="text-lg font-semibold text-gray-900">{formatCurrency(itemTotal)}</p>
          </div>
        </div>
      </div>
      
      {updating && (
        <div className="absolute inset-0 bg-white bg-opacity-50 flex items-center justify-center rounded-lg">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
      )}
    </div>
  );
};

export default CartItem;
