import React from 'react';
import ProductCard from './ProductCard';
import Spinner from '../common/Spinner';
import ErrorMessage from '../common/ErrorMessage';

/**
 * ProductList component for displaying grid of products
 * @param {Object} props
 * @param {Array} props.products - Array of product objects
 * @param {boolean} props.loading - Loading state
 * @param {string} props.error - Error message
 * @param {Function} props.onRetry - Retry function for errors
 */
const ProductList = ({ products, loading, error, onRetry }) => {
  // Loading State
  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <Spinner size="large" />
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="py-20">
        <ErrorMessage message={error} onRetry={onRetry} />
      </div>
    );
  }

  // Empty State
  if (!products || products.length === 0) {
    return (
      <div className="text-center py-20">
        <svg
          className="mx-auto h-24 w-24 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
          />
        </svg>
        <h3 className="mt-4 text-lg font-medium text-gray-900">No products found</h3>
        <p className="mt-2 text-sm text-gray-500">
          Try adjusting your filters or search criteria
        </p>
      </div>
    );
  }

  // Product Grid
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
};

export default ProductList;
