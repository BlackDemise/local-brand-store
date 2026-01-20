import React from 'react';
import { useNavigate } from 'react-router-dom';
import Card from '../common/Card';
import Badge from '../common/Badge';
import { formatCurrency } from '../../utils/formatters';

/**
 * ProductCard component for displaying product in grid/list view
 * @param {Object} props
 * @param {Object} props.product - Product object
 */
const ProductCard = ({ product }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/products/${product.slug}`);
  };

  // Calculate total stock across all SKUs
  const totalStock = product.skus?.reduce((sum, sku) => sum + sku.stock, 0) || 0;
  const isInStock = totalStock > 0;

  // Get the first available image or use placeholder
  const imageUrl = product.primaryImageUrl;

  return (
    <Card onClick={handleClick} className="h-full flex flex-col">
      {/* Product Image */}
      <div className="relative w-full pt-[100%] bg-gray-200 overflow-hidden">
        <img
          src={imageUrl}
          alt={product.name}
          className="absolute top-0 left-0 w-full h-full object-cover transition-transform duration-300 hover:scale-110"
          loading="lazy"
        />
        {/* Stock Badge */}
        {!isInStock && (
          <div className="absolute top-2 right-2">
            <Badge variant="danger">Out of Stock</Badge>
          </div>
        )}
      </div>

      {/* Product Info */}
      <div className="p-4 flex-grow flex flex-col">
        {/* Category Badge */}
        {product.category && (
          <div className="mb-2">
            <Badge variant="info">{product.category.name}</Badge>
          </div>
        )}

        {/* Product Name */}
        <h3 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
          {product.name}
        </h3>

        {/* Description Preview */}
        {product.description && (
          <p className="text-sm text-gray-600 mb-3 line-clamp-2 flex-grow">
            {product.description}
          </p>
        )}

        {/* Price and Stock */}
        <div className="flex items-center justify-between mt-auto">
          <div className="text-xl font-bold text-blue-600">
            {formatCurrency(product.basePrice)}
          </div>
          {isInStock && (
            <span className="text-sm text-green-600 font-medium">
              In Stock
            </span>
          )}
        </div>
      </div>
    </Card>
  );
};

export default ProductCard;
