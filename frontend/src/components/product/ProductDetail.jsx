import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ProductImage from './ProductImage';
import SkuSelector from './SkuSelector';
import Button from '../common/Button';
import Badge from '../common/Badge';
import { formatCurrency } from '../../utils/formatters';
import { useCart } from '../../hooks/useCart';

/**
 * ProductDetail component for displaying full product information
 * @param {Object} props
 * @param {Object} props.product - Product object with SKUs and images
 */
const ProductDetail = ({ product }) => {
  const navigate = useNavigate();
  const { addToCart, loading: cartLoading } = useCart();
  
  const [selectedSku, setSelectedSku] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState('');

  const handleSkuSelect = (sku) => {
    setSelectedSku(sku);
    setError('');
  };

  const handleQuantityChange = (newQuantity) => {
    const qty = parseInt(newQuantity);
    
    if (isNaN(qty) || qty < 1) {
      setQuantity(1);
      return;
    }
    
    if (selectedSku && qty > selectedSku.stock) {
      setQuantity(selectedSku.stock);
      setError(`Only ${selectedSku.stock} items available`);
      return;
    }
    
    setQuantity(qty);
    setError('');
  };

  const handleAddToCart = async () => {
    // Validation
    if (!selectedSku) {
      setError('Please select size and color');
      return;
    }

    if (selectedSku.stock === 0) {
      setError('This variant is out of stock');
      return;
    }

    if (quantity > selectedSku.stock) {
      setError(`Only ${selectedSku.stock} items available`);
      return;
    }

    try {
      await addToCart(selectedSku.id, quantity);
      // Reset after successful add
      setQuantity(1);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to add to cart');
    }
  };

  const handleCategoryClick = () => {
    if (product.category) {
      navigate(`/products?categoryId=${product.category.id}`);
    }
  };

  // Calculate total stock
  const totalStock = product.skus?.reduce((sum, sku) => sum + sku.stockQty, 0) || 0;
  const isInStock = totalStock > 0;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-12">
      {/* Left Column - Images */}
      <div>
        <ProductImage 
          images={product.imageUrls} 
          productName={product.name} 
        />
      </div>

      {/* Right Column - Product Info */}
      <div className="space-y-6">
        {/* Category Badge */}
        {product.category && (
          <div>
            <Badge 
              variant="info" 
              onClick={handleCategoryClick}
              className="cursor-pointer hover:bg-blue-100"
            >
              {product.category.name}
            </Badge>
          </div>
        )}

        {/* Product Name */}
        <h1 className="text-3xl lg:text-4xl font-bold text-gray-900">
          {product.name}
        </h1>

        {/* Price and Stock Status */}
        <div className="flex items-center justify-between pb-6 border-b border-gray-200">
          <div className="text-4xl font-bold text-blue-600">
            {formatCurrency(product.basePrice)}
          </div>
          <Badge variant={isInStock ? 'success' : 'danger'}>
            {isInStock ? 'In Stock' : 'Out of Stock'}
          </Badge>
        </div>

        {/* Description */}
        {product.description && (
          <div className="prose max-w-none">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Description</h3>
            <p className="text-gray-700 whitespace-pre-line">
              {product.description}
            </p>
          </div>
        )}

        {/* SKU Selector */}
        {product.skus && product.skus.length > 0 && (
          <div className="py-6 border-t border-gray-200">
            <SkuSelector
              skus={product.skus}
              selectedSku={selectedSku}
              onSelect={handleSkuSelect}
            />
          </div>
        )}

        {/* Quantity Selector */}
        <div className="py-6 border-t border-gray-200">
          <label className="block text-sm font-medium text-gray-700 mb-3">
            Quantity
          </label>
          <div className="flex items-center space-x-3">
            <button
              onClick={() => handleQuantityChange(quantity - 1)}
              disabled={quantity <= 1 || !selectedSku}
              className="w-10 h-10 flex items-center justify-center border border-gray-300 rounded-md hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              -
            </button>
            <input
              type="number"
              min="1"
              max={selectedSku?.stock || 999}
              value={quantity}
              onChange={(e) => handleQuantityChange(e.target.value)}
              disabled={!selectedSku}
              className="w-20 h-10 text-center border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
            <button
              onClick={() => handleQuantityChange(quantity + 1)}
              disabled={!selectedSku || quantity >= selectedSku.stock}
              className="w-10 h-10 flex items-center justify-center border border-gray-300 rounded-md hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              +
            </button>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Add to Cart Button */}
        <Button
          onClick={handleAddToCart}
          disabled={!selectedSku || selectedSku.stock === 0 || cartLoading}
          loading={cartLoading}
          variant="primary"
          className="w-full py-4 text-lg"
        >
          Add to Cart
        </Button>

        {/* Additional Info */}
        <div className="bg-gray-50 rounded-lg p-4 space-y-2 text-sm text-gray-600">
          <p>✓ Free shipping on orders over 500,000 ₫</p>
          <p>✓ 7-day return policy</p>
          <p>✓ Secure payment</p>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
