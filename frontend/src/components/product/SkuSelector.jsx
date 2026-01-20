import React, { useState, useEffect } from 'react';
import { formatCurrency } from '../../utils/formatters';

/**
 * SkuSelector component for selecting size and color variants
 * @param {Object} props
 * @param {Array} props.skus - Array of SKU objects
 * @param {Object} props.selectedSku - Currently selected SKU
 * @param {Function} props.onSelect - Callback when SKU is selected
 */
const SkuSelector = ({ skus = [], selectedSku, onSelect }) => {
  const [selectedSize, setSelectedSize] = useState(null);
  const [selectedColor, setSelectedColor] = useState(null);

  // Extract unique sizes and colors from SKUs
  const sizes = [...new Set(skus.map(sku => sku.size))].filter(Boolean);
  const colors = [...new Set(skus.map(sku => sku.color))].filter(Boolean);

  // Get stock quantity for a specific size (across all colors or for selected color)
  const getSizeStock = (size) => {
    if (!selectedColor) {
      // No color selected: sum stock across all colors for this size
      return skus
        .filter(sku => sku.size === size)
        .reduce((sum, sku) => sum + sku.stockQty, 0);
    }
    // Color selected: get stock for this specific size+color combination
    const sku = skus.find(s => s.size === size && s.color === selectedColor);
    return sku ? sku.stockQty : 0;
  };

  // Get stock quantity for a specific color (across all sizes or for selected size)
  const getColorStock = (color) => {
    if (!selectedSize) {
      // No size selected: sum stock across all sizes for this color
      return skus
        .filter(sku => sku.color === color)
        .reduce((sum, sku) => sum + sku.stockQty, 0);
    }
    // Size selected: get stock for this specific size+color combination
    const sku = skus.find(s => s.size === selectedSize && s.color === color);
    return sku ? sku.stockQty : 0;
  };

  // Check if size is available based on current color selection
  const isSizeAvailable = (size) => {
    return getSizeStock(size) > 0;
  };

  // Check if color is available based on current size selection
  const isColorAvailable = (color) => {
    return getColorStock(color) > 0;
  };

  // Handle size selection
  const handleSizeChange = (size) => {
    setSelectedSize(size);
    
    // Find matching SKU if color is also selected
    if (selectedColor) {
      const sku = skus.find(s => s.size === size && s.color === selectedColor);
      if (sku) {
        onSelect(sku);
      } else {
        // Reset color if combination not available
        setSelectedColor(null);
        onSelect(null);
      }
    }
  };

  // Handle color selection
  const handleColorChange = (color) => {
    setSelectedColor(color);
    
    // Find matching SKU if size is also selected
    if (selectedSize) {
      const sku = skus.find(s => s.size === selectedSize && s.color === color);
      if (sku) {
        onSelect(sku);
      } else {
        // Reset size if combination not available
        setSelectedSize(null);
        onSelect(null);
      }
    }
  };

  // Update local state when selectedSku prop changes
  useEffect(() => {
    if (selectedSku) {
      setSelectedSize(selectedSku.size);
      setSelectedColor(selectedSku.color);
    }
  }, [selectedSku]);

  return (
    <div className="space-y-6">
      {/* Size Selector */}
      {sizes.length > 0 && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-3">
            Size {selectedSize && <span className="text-gray-500">: {selectedSize}</span>}
          </label>
          <div className="flex flex-wrap gap-2">
            {sizes.map((size) => {
              const stock = getSizeStock(size);
              const isAvailable = isSizeAvailable(size);
              const isSelected = selectedSize === size;
              
              return (
                <button
                  key={size}
                  onClick={() => isAvailable && handleSizeChange(size)}
                  disabled={!isAvailable}
                  className={`
                    px-4 py-2 border-2 rounded-md font-medium transition-all
                    ${isSelected 
                      ? 'border-blue-600 bg-blue-50 text-blue-600' 
                      : 'border-gray-300 text-gray-700 hover:border-gray-400'
                    }
                    ${!isAvailable && 'opacity-50 cursor-not-allowed line-through'}
                  `}
                >
                  <div className="flex flex-col items-center">
                    <span>{size}</span>
                    <span className={`text-xs ${
                      isSelected ? 'text-blue-500' : 'text-gray-500'
                    }`}>
                      ({stock})
                    </span>
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Color Selector */}
      {colors.length > 0 && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-3">
            Color {selectedColor && <span className="text-gray-500">: {selectedColor}</span>}
          </label>
          <div className="flex flex-wrap gap-3">
            {colors.map((color) => {
              const stock = getColorStock(color);
              const isAvailable = isColorAvailable(color);
              const isSelected = selectedColor === color;
              
              return (
                <button
                  key={color}
                  onClick={() => isAvailable && handleColorChange(color)}
                  disabled={!isAvailable}
                  className={`
                    relative px-4 py-2 border-2 rounded-md font-medium transition-all
                    ${isSelected 
                      ? 'border-blue-600 bg-blue-50 text-blue-600' 
                      : 'border-gray-300 text-gray-700 hover:border-gray-400'
                    }
                    ${!isAvailable && 'opacity-50 cursor-not-allowed'}
                  `}
                >
                  <div className="flex flex-col items-center">
                    <span>{color}</span>
                    <span className={`text-xs ${
                      isSelected ? 'text-blue-500' : 'text-gray-500'
                    }`}>
                      ({stock})
                    </span>
                  </div>
                  {!isAvailable && (
                    <span className="absolute inset-0 flex items-center justify-center">
                      <span className="block w-full h-0.5 bg-red-500 rotate-45"></span>
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Selected SKU Info */}
      {selectedSku && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Selected Variant</p>
              <p className="font-semibold text-gray-900">
                {selectedSize} - {selectedColor}
              </p>
            </div>
            <div className="text-right">
              <p className="text-2xl font-bold text-blue-600">
                {formatCurrency(selectedSku.price)}
              </p>
              <p className={`text-sm font-medium ${
                selectedSku.stockQty > 10 ? 'text-green-600' : 
                selectedSku.stockQty > 0 ? 'text-yellow-600' : 'text-red-600'
              }`}>
                {selectedSku.stockQty > 0 
                  ? `${selectedSku.stockQty} items left` 
                  : 'Out of stock'
                }
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Validation Message */}
      {(!selectedSize || !selectedColor) && sizes.length > 0 && colors.length > 0 && (
        <p className="text-sm text-gray-500 italic">
          Please select both size and color
        </p>
      )}
    </div>
  );
};

export default SkuSelector;
